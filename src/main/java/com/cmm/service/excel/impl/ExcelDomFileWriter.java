package com.cmm.service.excel.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.cmm.service.excel.ExcelStyle;

/** Classpath XML의 고정 셀 및 반복 행을 Map 또는 JavaBean에 매핑한다. */
@Component
public class ExcelDomFileWriter {
    public byte[] write(Object data, String resource) throws IOException {
        return write(data, resource, null);
    }

    public byte[] write(Object data, String resource, ExcelStyle style) throws IOException {
        if (resource == null || resource.trim().isEmpty()) {
            throw new IllegalArgumentException("XML classpath 경로가 필요합니다.");
        }
        String path = resource.startsWith("classpath:") ? resource.substring(10) : resource;
        Document document;
        try (InputStream input = new ClassPathResource(path).getInputStream()) {
            document = parse(input);
        }
        Element root = document.getDocumentElement();
        if (!"excel".equals(root.getTagName()) || !"1".equals(root.getAttribute("version"))) {
            throw new IOException("엑셀 XML 루트는 <excel version=\"1\">이어야 합니다.");
        }
        ExcelStyle selectedStyle = style == null ? new DefaultExcelStyle() : style;
        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Map<String, CellStyle> styles = new HashMap<>();
            styles.put("header", selectedStyle.createHeaderStyle(workbook));
            styles.put("body", selectedStyle.createBodyStyle(workbook));
            List<Element> sheets = children(root);
            if (sheets.isEmpty()) throw new IOException("엑셀 XML에 sheet가 필요합니다.");
            for (Element definition : sheets) {
                requireTag(definition, "sheet");
                Sheet sheet = workbook.createSheet(WorkbookUtil.createSafeSheetName(required(definition, "name")));
                sheet.setDefaultRowHeightInPoints(24);
                int offset = 0;
                for (Element element : children(definition)) {
                	
                    switch (element.getTagName()) {
                    
                    case "column":
                        int index = Integer.parseInt(required(element, "index")) - 1;
                        if (index < 0 || index >= SpreadsheetVersion.EXCEL2007.getMaxColumns()) {
                            throw new IOException("열 번호가 범위를 벗어났습니다.");
                        }
                        int width = element.hasAttribute("width")
                                ? (int) (Double.parseDouble(element.getAttribute("width")) * 256)
                                : selectedStyle.getColumnWidth(index);
                        sheet.setColumnWidth(index, width);
                        break;
                        
                    case "cell":
                        put(workbook, sheet, element, data, offset, styles);
                        break;
                        
                    case "repeat":
                        Object items = property(data, required(element, "items"));
                        if (items != null && !(items instanceof Iterable<?>)) {
                            throw new IOException("repeat items는 Iterable이어야 합니다.");
                        }
                        List<Element> cells = children(element);
                        if (cells.isEmpty()) throw new IOException("repeat에 cell이 필요합니다.");
                        int repeatRow = -1;
                        for (Element cell : cells) {
                            requireTag(cell, "cell");
                            CellRangeAddress cellRange = CellRangeAddress.valueOf(required(cell, "range"));
                            if (cellRange.getFirstRow() != cellRange.getLastRow()
                                    || (repeatRow >= 0 && repeatRow != cellRange.getFirstRow())) {
                                throw new IOException("repeat의 셀은 같은 한 행에 있어야 합니다.");
                            }
                            repeatRow = cellRange.getFirstRow();
                        }
                        if (items != null) {
                            for (Object item : (Iterable<?>) items) {
                                for (Element cell : cells) {
                                    requireTag(cell, "cell");
                                    put(workbook, sheet, cell, item, offset, styles);
                                }
                                offset++;
                            }
                        }
                        break;
                        
                    default:
                        throw new IOException("지원하지 않는 XML 요소: " + element.getTagName());
                    }
                }
            }
            workbook.write(output);
            return output.toByteArray();
        } catch (IllegalArgumentException exception) {
            throw new IOException("엑셀 XML 매핑이 올바르지 않습니다: " + exception.getMessage(), exception);
        }
    }

    private Document parse(InputStream input) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new DefaultHandler() {
                @Override public void error(SAXParseException e) throws SAXException { throw e; }
                @Override public void fatalError(SAXParseException e) throws SAXException { throw e; }
            });
            return builder.parse(input);
        } catch (javax.xml.parsers.ParserConfigurationException | SAXException | IllegalArgumentException e) {
            throw new IOException("엑셀 XML을 읽을 수 없습니다.", e);
        }
    }

    private void put(Workbook workbook, Sheet sheet, Element element, Object data, int offset,
            Map<String, CellStyle> styles) throws IOException {
        String reference = required(element, "range");
        if (!reference.matches("[A-Z]+[1-9][0-9]*(?::[A-Z]+[1-9][0-9]*)?")) {
            throw new IOException("잘못된 셀 범위: " + reference);
        }
        CellRangeAddress range = CellRangeAddress.valueOf(reference);
        range.setFirstRow(range.getFirstRow() + offset);
        range.setLastRow(range.getLastRow() + offset);
        range.validate(SpreadsheetVersion.EXCEL2007);
        String base = element.hasAttribute("style") ? element.getAttribute("style")
                : element.hasAttribute("text") ? "header" : "body";
        if (!styles.containsKey(base) || !("header".equals(base) || "body".equals(base))) {
            throw new IOException("지원하지 않는 셀 스타일: " + base);
        }
        boolean hasProperty = element.hasAttribute("property");
        if (hasProperty == element.hasAttribute("text")) {
            throw new IOException("cell에는 text 또는 property 중 하나가 필요합니다: " + reference);
        }
        Object value = hasProperty ? property(data, required(element, "property")) : element.getAttribute("text");
        String format = element.getAttribute("format");
        if (format.isEmpty() && (value instanceof Date || value instanceof LocalDateTime)) {
            format = "yyyy-mm-dd hh:mm";
        } else if (format.isEmpty() && value instanceof LocalDate) {
            format = "yyyy-mm-dd";
        }
        String alignment = element.getAttribute("align");
        String key = base + "|" + format + "|" + alignment;
        CellStyle cellStyle = styles.get(key);
        if (cellStyle == null) {
            cellStyle = workbook.createCellStyle();
            cellStyle.cloneStyleFrom(styles.get(base));
            cellStyle.setWrapText(true);
            if (!format.isEmpty()) cellStyle.setDataFormat(workbook.createDataFormat().getFormat(format));
            if (!alignment.isEmpty()) cellStyle.setAlignment(HorizontalAlignment.valueOf(alignment.toUpperCase(java.util.Locale.ROOT)));
            styles.put(key, cellStyle);
        }
        for (int rowIndex = range.getFirstRow(); rowIndex <= range.getLastRow(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) row = sheet.createRow(rowIndex);
            if (element.hasAttribute("height")) {
                float height = Float.parseFloat(element.getAttribute("height"));
                if (!Float.isFinite(height) || height <= 0 || height > 409) throw new IOException("잘못된 행 높이");
                row.setHeightInPoints(height);
            }
            for (int column = range.getFirstColumn(); column <= range.getLastColumn(); column++) {
                if (row.getCell(column) != null) throw new IOException("중복된 셀 범위: " + reference);
                row.createCell(column).setCellStyle(cellStyle);
            }
        }
        Cell cell = sheet.getRow(range.getFirstRow()).getCell(range.getFirstColumn());
        if (value instanceof Number) cell.setCellValue(((Number) value).doubleValue());
        else if (value instanceof Boolean) cell.setCellValue((Boolean) value);
        else if (value instanceof LocalDateTime) cell.setCellValue(java.sql.Timestamp.valueOf((LocalDateTime) value));
        else if (value instanceof LocalDate) cell.setCellValue(java.sql.Date.valueOf((LocalDate) value));
        else if (value instanceof Date) cell.setCellValue((Date) value);
        else if (value != null) cell.setCellValue(String.valueOf(value));
        if (range.getNumberOfCells() > 1) sheet.addMergedRegion(range);
    }

    private Object property(Object data, String path) throws IOException {
        Object value = data;
        for (String part : path.split("\\.")) {
            if (value == null) return null;
            if (value instanceof Map<?, ?>) {
                Map<?, ?> map = (Map<?, ?>) value;
                if (!map.containsKey(part)) throw new IOException("매핑 속성을 찾을 수 없습니다: " + path);
                value = map.get(part);
            } else {
                BeanWrapper bean = PropertyAccessorFactory.forBeanPropertyAccess(value);
                if (!bean.isReadableProperty(part) || "class".equals(part)) {
                    throw new IOException("매핑 속성을 읽을 수 없습니다: " + path);
                }
                value = bean.getPropertyValue(part);
            }
        }
        return value;
    }

    private List<Element> children(Element parent) {
        List<Element> result = new ArrayList<>();
        for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node instanceof Element) result.add((Element) node);
        }
        return result;
    }

    private String required(Element element, String attribute) throws IOException {
        String value = element.getAttribute(attribute).trim();
        if (value.isEmpty()) throw new IOException(element.getTagName() + "의 " + attribute + "가 필요합니다.");
        return value;
    }

    private void requireTag(Element element, String tag) throws IOException {
        if (!tag.equals(element.getTagName())) throw new IOException("XML 요소가 올바르지 않습니다: " + element.getTagName());
    }
}
