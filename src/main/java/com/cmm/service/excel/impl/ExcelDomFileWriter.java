package com.cmm.service.excel.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
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
        return write(data, template, style, null);
    }
    public byte[] write(Object data, Definition template, ExcelStyle style, Map<String, java.util.List<String>> dropdowns) throws IOException {
        if (template == null) throw new IllegalArgumentException("엑셀 매핑 양식이 필요합니다.");
        ExcelStyle selected = style == null ? new DefaultExcelStyle() : style;
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Map<String, CellStyle> styles = new HashMap<>();
            styles.put("header", selected.createHeaderStyle(workbook));
            styles.put("body", selected.createBodyStyle(workbook));
            for (SheetMapping definition : template.getSheets()) {
                Sheet sheet = workbook.createSheet(definition.getName());
                sheet.setDefaultRowHeightInPoints(definition.getDefaultRowHeight());
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
            if (dropdowns != null) applyDropdowns(workbook, template, dropdowns);
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
        String key = mapping.getStyle() + "|" + format + "|" + mapping.getAlignment() + "|" + mapping.getVertical();
        CellStyle style = styles.get(key);
        if (style == null) {
            style = workbook.createCellStyle();
            style.cloneStyleFrom(styles.get(mapping.getStyle()));
            style.setWrapText(true);
            if (!format.isEmpty()) style.setDataFormat(workbook.createDataFormat().getFormat(format));
            if (mapping.getAlignment() != null) style.setAlignment(mapping.getAlignment());
            if (mapping.getVertical() != null) style.setVerticalAlignment(mapping.getVertical());
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
    
    /** XML에 지정된 셀에 숨김 목록과 이름 정의를 연결한다. */
    private void applyDropdowns(Workbook workbook, Definition template, Map<String, List<String>> options) throws IOException {
        Sheet lookup = null;
        Map<String, String> names = new LinkedHashMap<>();
        for (SheetMapping definition : template.getSheets()) {
            Sheet target = workbook.getSheet(definition.getName());
            for (Mapping item : definition.getMappings()) {
                if (!(item instanceof CellMapping)) continue;
                CellMapping cell = (CellMapping) item;
                String key = cell.getDropdown();
                if (key.isEmpty()) continue;
                List<String> values = options.get(key);
                if (values == null || values.isEmpty()) throw new IOException("드롭다운 코드 목록이 없습니다: " + key);
                String name = names.get(key);
                if (name == null) {
                    if (lookup == null) {
                        String sheetName = "_excel_codes";
                        int suffix = 1;
                        while (workbook.getSheet(sheetName) != null) sheetName = "_excel_codes_" + suffix++;
                        lookup = workbook.createSheet(sheetName);
                        workbook.setSheetVisibility(workbook.getSheetIndex(lookup), SheetVisibility.VERY_HIDDEN);
                    }
                    LinkedHashSet<String> unique = new LinkedHashSet<>();
                    for (String value : values) {
                        if (value == null || value.trim().isEmpty()) throw new IOException("빈 코드는 드롭다운에 사용할 수 없습니다: " + key);
                        unique.add(value);
                    }
                    
                    if (unique.size() > workbook.getSpreadsheetVersion().getMaxRows()) throw new IOException("드롭다운 항목 수가 너무 많습니다.");
                    int column = names.size(), index = 0;
                    if (column >= workbook.getSpreadsheetVersion().getMaxColumns()) throw new IOException("드롭다운 목록 수가 너무 많습니다.");
                    
                    for (String value : unique) {
                        Row row = lookup.getRow(index);
                        if (row == null) row = lookup.createRow(index);
                        row.createCell(column).setCellValue(value); index++;
                    }
                    
                    int number = names.size() + 1;
                    do { name = "_excelChoice" + number++; } while (workbook.getName(name) != null);
                    Name defined = workbook.createName(); defined.setNameName(name);
                    String col = org.apache.poi.ss.util.CellReference.convertNumToColString(column);
                    defined.setRefersToFormula("'" + lookup.getSheetName().replace("'", "''") + "'!$" + col + "$1:$" + col + "$" + unique.size());
                    names.put(key, name);
                }
                CellRangeAddress range = cell.range(0);
                DataValidationHelper helper = target.getDataValidationHelper();
                // 병합된 셀은 왼쪽 위 셀에 값을 보관하므로 해당 셀에만 검증을 연결한다.
                DataValidation validation = helper.createValidation(helper.createFormulaListConstraint(name),
                        new CellRangeAddressList(range.getFirstRow(), range.getFirstRow(), range.getFirstColumn(), range.getFirstColumn()));
                validation.setEmptyCellAllowed(true);
                validation.setSuppressDropDownArrow(true);
                validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
                validation.setShowErrorBox(true);
                validation.createErrorBox("코드 입력 확인", "목록에 있는 항목을 선택해주세요.");
                validation.setShowPromptBox(true);
                validation.createPromptBox("항목 선택", "한국어 이름을 선택해주세요. 등록 시 코드로 변환됩니다.");
                target.addValidationData(validation);
            }
        }
    }
}
