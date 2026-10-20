package com.event.service.impl;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;

import com.cmm.service.excel.impl.DefaultExcelStyle;

/** sample.xlsx의 글꼴, 회색 제목 배경과 검은 테두리를 적용한다. */
public class EventExcelStyle extends DefaultExcelStyle {
    @Override
    public CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = super.createHeaderStyle(workbook);
        Font font = workbook.createFont();
        font.setFontName("맑은 고딕");
        font.setFontHeightInPoints((short) 10);
        font.setBold(true);
        style.setFont(font);
        ((XSSFCellStyle) style).setFillForegroundColor(new XSSFColor(
                new byte[] {(byte) 217, (byte) 217, (byte) 217}, new DefaultIndexedColorMap()));
        return style;
    }

    @Override
    public CellStyle createBodyStyle(Workbook workbook) {
        CellStyle style = super.createBodyStyle(workbook);
        Font font = workbook.createFont();
        font.setFontName("맑은 고딕");
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    @Override
    protected void applyBorder(CellStyle style) {
        super.applyBorder(style);
        short black = IndexedColors.BLACK.getIndex();
        style.setTopBorderColor(black);
        style.setBottomBorderColor(black);
        style.setLeftBorderColor(black);
        style.setRightBorderColor(black);
    }
}
