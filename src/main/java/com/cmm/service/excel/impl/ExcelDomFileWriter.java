package com.cmm.service.excel.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.stereotype.Component;
import com.cmm.service.excel.ExcelStyle;
import com.cmm.service.excel.ExcelTemplate;
import com.cmm.service.excel.ExcelTemplate.*;

/** XML 자체를 해석하지 않고 공통 Template의 매핑 정보로 엑셀을 생성한다. */
@Component
public class ExcelDomFileWriter {
    public byte[] write(Object data, String resource) throws IOException {
        return write(data, resource, null);
    }
    /** 기존 리소스 경로 호출 호환용. XML 해석은 ExcelTemplate에 위임한다. */
    public byte[] write(Object data, String resource, ExcelStyle style) throws IOException {
        return write(data, new ExcelTemplate().load(resource), style);
    }
    public byte[] write(Object data, Definition template, ExcelStyle style) throws IOException {
        if (template == null) throw new IllegalArgumentException("엑셀 매핑 양식이 필요합니다.");
        ExcelStyle selected = style == null ? new DefaultExcelStyle() : style;
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Map<String, CellStyle> styles = new HashMap<>();
            styles.put("header", selected.createHeaderStyle(workbook));
            styles.put("body", selected.createBodyStyle(workbook));
            for (SheetMapping definition : template.getSheets()) {
                Sheet sheet = workbook.createSheet(definition.getName());
                sheet.setDefaultRowHeightInPoints(24);
                int offset = 0;
                for (Mapping mapping : definition.getMappings()) {
                    if (mapping instanceof ColumnMapping) {
                        ColumnMapping column = (ColumnMapping) mapping;
                        sheet.setColumnWidth(column.getIndex(), column.getWidth() == null
                                ? selected.getColumnWidth(column.getIndex()) : column.getWidth());
                    } else if (mapping instanceof CellMapping) {
                        put(workbook, sheet, (CellMapping) mapping, data, offset, styles);
                    } else if (mapping instanceof RepeatMapping) {
                        RepeatMapping repeat = (RepeatMapping) mapping;
                        Object items = property(data, repeat.getItems());
                        if (items != null && !(items instanceof Iterable<?>)) throw new IOException("repeat items는 Iterable이어야 합니다.");
                        if (items != null) for (Object item : (Iterable<?>) items) {
                            for (CellMapping cell : repeat.getCells()) put(workbook, sheet, cell, item, offset, styles);
                            offset++;
                        }
                    }
                }
            }
            workbook.write(output);
            return output.toByteArray();
        } catch (IllegalArgumentException e) {
            throw new IOException("엑셀 매핑에 실패했습니다.", e);
        }
    }

    private void put(Workbook workbook, Sheet sheet, CellMapping mapping, Object data, int offset,
            Map<String, CellStyle> styles) throws IOException {
        CellRangeAddress range = mapping.range(offset);
        Object value = mapping.getProperty() == null ? mapping.getText() : property(data, mapping.getProperty());
        String format = mapping.getFormat();
        if (format.isEmpty() && (value instanceof Date || value instanceof LocalDateTime)) format = "yyyy-mm-dd hh:mm";
        else if (format.isEmpty() && value instanceof LocalDate) format = "yyyy-mm-dd";
        String key = mapping.getStyle() + "|" + format + "|" + mapping.getAlignment();
        CellStyle style = styles.get(key);
        if (style == null) {
            style = workbook.createCellStyle();
            style.cloneStyleFrom(styles.get(mapping.getStyle()));
            style.setWrapText(true);
            if (!format.isEmpty()) style.setDataFormat(workbook.createDataFormat().getFormat(format));
            if (mapping.getAlignment() != null) style.setAlignment(mapping.getAlignment());
            styles.put(key, style);
        }
        for (int rowIndex = range.getFirstRow(); rowIndex <= range.getLastRow(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) row = sheet.createRow(rowIndex);
            if (mapping.getHeight() != null) row.setHeightInPoints(mapping.getHeight());
            for (int column = range.getFirstColumn(); column <= range.getLastColumn(); column++) {
                if (row.getCell(column) != null) throw new IOException("중복된 셀 범위: " + mapping.getReference());
                row.createCell(column).setCellStyle(style);
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
                if (!bean.isReadableProperty(part) || "class".equals(part)) throw new IOException("매핑 속성을 읽을 수 없습니다: " + path);
                value = bean.getPropertyValue(part);
            }
        }
        return value;
    }
}
