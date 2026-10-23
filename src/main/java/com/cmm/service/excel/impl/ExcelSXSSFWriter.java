package com.cmm.service.excel.impl;

import java.io.IOException;
import java.util.Date;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;
import com.cmm.service.excel.ExcelStyle;

/** SXSSF 스트리밍 방식의 목록 엑셀 출력 전용. */
@Component
public class ExcelSXSSFWriter {
    /** 목록은 SXSSF의 행 버퍼로 생성하고 응답 스트림에 직접 기록한다. */
    public <T> void writeList(java.io.OutputStream output, String sheetName,
            String[] headers, int[] widths, Iterable<T> items,
            java.util.function.Function<T, Object[]> values, ExcelStyle style) throws IOException {
        ExcelStyle selected = style == null ? new DefaultExcelStyle() : style;
        org.apache.poi.xssf.streaming.SXSSFWorkbook workbook = new org.apache.poi.xssf.streaming.SXSSFWorkbook(100);
        try (org.apache.poi.xssf.streaming.SXSSFWorkbook closeable = workbook) {
            workbook.setCompressTempFiles(true);
            Sheet sheet = workbook.createSheet(sheetName);
            sheet.createFreezePane(0, 1);
            CellStyle headerStyle = selected.createHeaderStyle(workbook);
            CellStyle bodyStyle = selected.createBodyStyle(workbook);
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.cloneStyleFrom(bodyStyle);
            dateStyle.setDataFormat(workbook.createDataFormat().getFormat("yyyy-mm-dd hh:mm"));
            Row header = sheet.createRow(0);
            for (int column = 0; column < headers.length; column++) {
                sheet.setColumnWidth(column, widths[column] * 256);
                Cell cell = header.createCell(column);
                cell.setCellValue(headers[column]);
                cell.setCellStyle(headerStyle);
            }
            int index = 1;
            for (T item : items) {
                if (index >= workbook.getSpreadsheetVersion().getMaxRows())
                    throw new IOException("엑셀 최대 행 수를 초과했습니다.");
                Row row = sheet.createRow(index++);
                Object[] fields = values.apply(item);
                if (fields.length != headers.length) throw new IOException("엑셀 열 수가 일치하지 않습니다.");
                for (int column = 0; column < fields.length; column++) {
                    Cell cell = row.createCell(column);
                    Object value = fields[column];
                    cell.setCellStyle(value instanceof Date ? dateStyle : bodyStyle);
                    if (value instanceof Date) cell.setCellValue((Date) value);
                    else if (value instanceof Number) cell.setCellValue(((Number) value).doubleValue());
                    else if (value != null) cell.setCellValue(String.valueOf(value));
                }
            }
            workbook.write(output);
        } finally {
            workbook.dispose();
        }
    }

}
