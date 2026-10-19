package com.cmm.service.excel.impl;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import com.cmm.service.excel.ExcelStyle;

@Component
public class DefaultExcelStyle implements ExcelStyle {

    @Override
    public CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);

        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        applyBorder(style);

        return style;
    }

    @Override
    public CellStyle createBodyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);

        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        applyBorder(style);

        return style;
    }

    @Override
    public int getColumnWidth(int columnIndex) {
        return 20 * 256;
    }

    protected void applyBorder(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        short borderColor = IndexedColors.GREY_25_PERCENT.getIndex();

        style.setTopBorderColor(borderColor);
        style.setBottomBorderColor(borderColor);
        style.setLeftBorderColor(borderColor);
        style.setRightBorderColor(borderColor);
    }
}