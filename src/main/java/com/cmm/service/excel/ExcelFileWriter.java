package com.cmm.service.excel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.util.WorkbookUtil;
import org.springframework.stereotype.Component;

@Component
public class ExcelFileWriter {

    // 기본 스타일로 생성
    public <T> byte[] write(
            List<T> data,
            ExcelTemplate<T> template
    ) throws IOException {
        return write(data, template, null);
    }

    // 사용자 정의 스타일로 생성
    public <T> byte[] write(
            List<T> data,
            ExcelTemplate<T> template,
            ExcelStyle style
    ) throws IOException {

        if (template == null || template.getHeaders() == null
                || template.getHeaders().isEmpty()) {
            throw new IllegalArgumentException("엑셀 양식이 올바르지 않습니다.");
        }

        if (data == null) data = Collections.emptyList();

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            String sheetName = WorkbookUtil.createSafeSheetName(template.getSheetName());
            Sheet sheet = workbook.createSheet(sheetName);
            List<String> headers = template.getHeaders();

            CellStyle headerStyle = style != null
                ? style.createHeaderStyle(workbook)
                : createDefaultHeaderStyle(workbook);

            CellStyle bodyStyle = style != null
                ? style.createBodyStyle(workbook)
                : workbook.createCellStyle();

            // 날짜 스타일
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.cloneStyleFrom(bodyStyle);
            dateStyle.setDataFormat(workbook.createDataFormat().getFormat("yyyy-mm-dd"));

            CellStyle dateTimeStyle = workbook.createCellStyle();
            dateTimeStyle.cloneStyleFrom(bodyStyle);
            dateTimeStyle.setDataFormat(workbook.createDataFormat().getFormat("yyyy-mm-dd hh:mm"));

            // 헤더 작성
            Row headerRow = sheet.createRow(0);

            for (int col = 0; col < headers.size(); col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers.get(col));
                cell.setCellStyle(headerStyle);

                int width = style != null ? style.getColumnWidth(col) : 20 * 256;
                sheet.setColumnWidth(col, width);
            }

            // 데이터 작성
            int rowIndex = 1;

            for (T item : data) {

                List<?> values = template.writeRow(item);

                if (values == null || values.size() != headers.size()) {
                    throw new IllegalArgumentException("엑셀 컬럼 수가 일치하지 않습니다.");
                }

                Row row = sheet.createRow(rowIndex++);

                for (int col = 0; col < values.size(); col++) {
                    Cell cell = row.createCell(col);
                    Object value = values.get(col);

                    cell.setCellStyle(bodyStyle);
                    setCellValue(cell, value, dateStyle, dateTimeStyle);
                }
            }
            workbook.write(out);
            return out.toByteArray();
        }
    }

    // 빈 엑셀 양식 생성
    public <T> byte[] writeTemplate(
            ExcelTemplate<T> template,
            ExcelStyle style
    ) throws IOException {
        return write(Collections.<T>emptyList(), template, style);
    }

    private void setCellValue(
            Cell cell,
            Object value,
            CellStyle dateStyle,
            CellStyle dateTimeStyle
    ) {

        if (value == null) return;

        if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());

        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);

        } else if (value instanceof LocalDateTime) {
            cell.setCellValue(java.sql.Timestamp.valueOf((LocalDateTime) value));
            cell.setCellStyle(dateTimeStyle);

        } else if (value instanceof LocalDate) {
            cell.setCellValue(java.sql.Date.valueOf((LocalDate) value));
            cell.setCellStyle(dateStyle);

        } else if (value instanceof Date) {
            cell.setCellValue((Date) value);
            cell.setCellStyle(dateStyle);

        } else {
            cell.setCellValue(String.valueOf(value));
        }
    }

    private CellStyle createDefaultHeaderStyle(Workbook workbook) {

        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();

        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        return style;
    }
}