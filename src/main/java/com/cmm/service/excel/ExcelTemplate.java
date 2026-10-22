package com.cmm.service.excel;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.WorkbookUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/** XML 해석은 이 클래스만 담당한다. Writer/Parser는 동일한 불변 Definition을 참조한다. */
@Component
public class ExcelTemplate {
	
    public Definition load(String resource) throws IOException {
        if (resource == null || resource.trim().isEmpty()) {
            throw new IllegalArgumentException("XML classpath 경로가 필요합니다.");
        }
        String path = resource.startsWith("classpath:") ? resource.substring(10) : resource;
        try (InputStream input = new ClassPathResource(path).getInputStream()) {
            Element root = parse(input);
            if (!"excel".equals(root.getTagName()) || !"1".equals(root.getAttribute("version"))) {
                throw new IOException("엑셀 XML 루트는 <excel version=\"1\">이어야 합니다.");
            }
            List<SheetMapping> sheets = new ArrayList<>();
            Set<String> names = new HashSet<>();
            for (Element sheet : children(root)) {
                requireTag(sheet, "sheet");
                String name = WorkbookUtil.createSafeSheetName(required(sheet, "name"));
                
                if (!names.add(name.toLowerCase(Locale.ROOT))) throw new IOException("중복된 시트 이름: " + name);
                List<Mapping> mappings = new ArrayList<>();
                Set<Integer> columns = new HashSet<>();
                
                for (Element element : children(sheet)) {
                	
                    switch (element.getTagName()) {
                    
                    case "column":
                        int index = Integer.parseInt(required(element, "index")) - 1;
                        if (index < 0 || index >= SpreadsheetVersion.EXCEL2007.getMaxColumns() || !columns.add(index)) {
                            throw new IOException("잘못되거나 중복된 열 번호");
                        }
                        Integer width = null;
                        if (element.hasAttribute("width")) {
                            double characters = Double.parseDouble(required(element, "width"));
                            if (!Double.isFinite(characters) || characters <= 0 || characters > 255) {
                                throw new IOException("열 너비는 0 초과 255 이하여야 합니다.");
                            }
                            width = (int) (characters * 256);
                        }
                        mappings.add(new ColumnMapping(index, width));
                        break;
                        
                    case "cell": 
                    	mappings.add(cell(element)); break;
                    
                    case "repeat":
                        List<CellMapping> cells = new ArrayList<>();
                        int row = -1;
                        for (Element child : children(element)) {
                            requireTag(child, "cell");
                            CellMapping mapping = cell(child);
                            CellRangeAddress range = mapping.range(0);
                            if (!mapping.getDropdown().isEmpty()) throw new IOException("반복 셀에는 dropdown을 지정할 수 없습니다.");
                            if (range.getFirstRow() != range.getLastRow()
                                    || (row >= 0 && row != range.getFirstRow())) {
                                throw new IOException("repeat의 셀은 같은 한 행에 있어야 합니다.");
                            }
                            row = range.getFirstRow();
                            cells.add(mapping);
                        }
                        if (cells.isEmpty()) throw new IOException("repeat에 cell이 필요합니다.");
                        validateOverlap(cells);
                        mappings.add(new RepeatMapping(property(element, "items"), cells));
                        break;
                        
                    default: 
                    	throw new IOException("지원하지 않는 XML 요소: " + element.getTagName());
                    	
                    }
                }
                
                List<CellMapping> fixed = new ArrayList<>();
                
                for (Mapping mapping : mappings) if (mapping instanceof CellMapping) fixed.add((CellMapping) mapping);
                validateOverlap(fixed);
                
                if (fixed.isEmpty() && mappings.stream().noneMatch(m -> m instanceof RepeatMapping)) {
                    throw new IOException("시트에 cell 또는 repeat가 필요합니다.");
                }
                float rowHeight = sheet.hasAttribute("defaultRowHeight")
                        ? Float.parseFloat(required(sheet, "defaultRowHeight")) : 24;
                if (!Float.isFinite(rowHeight) || rowHeight <= 0 || rowHeight > 409) throw new IOException("잘못된 기본 행 높이");
                sheets.add(new SheetMapping(name, mappings, rowHeight));
            }
            
            if (sheets.isEmpty()) throw new IOException("엑셀 XML에 sheet가 필요합니다.");
            return new Definition(path, sheets);
            
        } catch (IllegalArgumentException exception) {
            throw new IOException("엑셀 XML 매핑이 올바르지 않습니다: " + resource, exception);
        }
    }

    private CellMapping cell(Element element) throws IOException {
        String reference = required(element, "range");
        if (!reference.matches("[A-Z]+[1-9][0-9]*(?::[A-Z]+[1-9][0-9]*)?")) throw new IOException("잘못된 셀 범위: " + reference);
        CellRangeAddress range = CellRangeAddress.valueOf(reference);
        range.validate(SpreadsheetVersion.EXCEL2007);
        boolean hasProperty = element.hasAttribute("property");
        if (hasProperty == element.hasAttribute("text")) throw new IOException("cell에는 text 또는 property 중 하나가 필요합니다.");
        String style = element.hasAttribute("style") ? required(element, "style") : hasProperty ? "body" : "header";
        if (!"header".equals(style) && !"body".equals(style)) throw new IOException("지원하지 않는 셀 스타일: " + style);
        HorizontalAlignment alignment = element.hasAttribute("align")
                ? HorizontalAlignment.valueOf(required(element, "align").toUpperCase(Locale.ROOT)) : null;
        VerticalAlignment vertical = element.hasAttribute("vertical")
                ? VerticalAlignment.valueOf(required(element, "vertical").toUpperCase(Locale.ROOT)) : null;
        Float height = null;
        if (element.hasAttribute("height")) {
            height = Float.valueOf(required(element, "height"));
            if (!Float.isFinite(height) || height <= 0 || height > 409) throw new IOException("잘못된 행 높이");
        }
        String dropdown = element.getAttribute("dropdown").trim();
        if (!dropdown.isEmpty() && (!hasProperty || !dropdown.matches("[A-Za-z_][A-Za-z0-9_]*")))
            throw new IOException("dropdown은 속성 셀의 코드 목록 키여야 합니다.");
        return new CellMapping(reference, hasProperty ? property(element, "property") : null,
                hasProperty ? null : element.getAttribute("text"), style, element.getAttribute("format"), alignment, vertical, height, dropdown);
    }

    private String property(Element element, String attribute) throws IOException {
        String value = required(element, attribute);
        if (!value.matches("[A-Za-z_][A-Za-z0-9_]*(?:\\.[A-Za-z_][A-Za-z0-9_]*)*")) throw new IOException("잘못된 속성 경로: " + value);
        for (String part : value.split("\\.")) if ("class".equals(part)) throw new IOException("class 속성은 매핑할 수 없습니다.");
        return value;
    }

    private void validateOverlap(List<CellMapping> cells) throws IOException {
        for (int i = 0; i < cells.size(); i++) {
            for (int j = 0; j < i; j++) {
                if (cells.get(i).range(0).intersects(cells.get(j).range(0))) {
                    throw new IOException("중복된 셀 범위: " + cells.get(i).getReference());
                }
            }
        }
    }

    private Element parse(InputStream input) throws IOException {
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
            return builder.parse(input).getDocumentElement();
        } catch (javax.xml.parsers.ParserConfigurationException | SAXException | IllegalArgumentException e) {
            throw new IOException("엑셀 XML을 읽을 수 없습니다.", e);
        }
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
        if (!tag.equals(element.getTagName())) throw new IOException("잘못된 XML 요소: " + element.getTagName());
    }

    public interface Mapping { }
    public static final class Definition {
        private final String resource;
        private final List<SheetMapping> sheets;
        private Definition(String resource, List<SheetMapping> sheets) {
            this.resource = resource; this.sheets = Collections.unmodifiableList(new ArrayList<>(sheets));
        }
        public String getResource() { return resource; }
        public List<SheetMapping> getSheets() { return sheets; }
    }
    public static final class SheetMapping {
        private final String name;
        private final List<Mapping> mappings;
        private final float defaultRowHeight;
        private SheetMapping(String name, List<Mapping> mappings, float defaultRowHeight) {
            this.defaultRowHeight = defaultRowHeight;
            this.name = name; this.mappings = Collections.unmodifiableList(new ArrayList<>(mappings));
        }
        public String getName() { return name; }
        public List<Mapping> getMappings() { return mappings; }
        public float getDefaultRowHeight() { return defaultRowHeight; }
    }
    public static final class ColumnMapping implements Mapping {
        private final int index;
        private final Integer width;
        private ColumnMapping(int index, Integer width) { this.index = index; this.width = width; }
        public int getIndex() { return index; }
        public Integer getWidth() { return width; }
    }
    public static final class RepeatMapping implements Mapping {
        private final String items;
        private final List<CellMapping> cells;
        private RepeatMapping(String items, List<CellMapping> cells) {
            this.items = items; this.cells = Collections.unmodifiableList(new ArrayList<>(cells));
        }
        public String getItems() { return items; }
        public List<CellMapping> getCells() { return cells; }
    }
    public static final class CellMapping implements Mapping {
        private final String reference, property, text, style, format, dropdown;
        private final HorizontalAlignment alignment;
        private final VerticalAlignment vertical;
        private final Float height;
        private CellMapping(String reference, String property, String text, String style, String format,
                HorizontalAlignment alignment, VerticalAlignment vertical, Float height, String dropdown) {
            this.dropdown = dropdown;
            this.reference = reference; this.property = property; this.text = text;
            this.style = style; this.format = format; this.alignment = alignment; this.vertical = vertical; this.height = height;
        }
        public String getDropdown() { return dropdown; }
        public String getReference() { return reference; }
        public String getProperty() { return property; }
        public String getText() { return text; }
        public String getStyle() { return style; }
        public String getFormat() { return format; }
        public HorizontalAlignment getAlignment() { return alignment; }
        public VerticalAlignment getVertical() { return vertical; }
        public Float getHeight() { return height; }
        public CellRangeAddress range(int offset) {
            CellRangeAddress range = CellRangeAddress.valueOf(reference);
            range.setFirstRow(Math.addExact(range.getFirstRow(), offset));
            range.setLastRow(Math.addExact(range.getLastRow(), offset));
            range.validate(SpreadsheetVersion.EXCEL2007);
            return range;
        }
    }
}
