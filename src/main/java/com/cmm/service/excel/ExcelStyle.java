package com.cmm.service.excel;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;

public interface ExcelStyle {
    // 헤더 스타일
    CellStyle createHeaderStyle(Workbook workbook);

    // 데이터 스타일
    CellStyle createBodyStyle(Workbook workbook);

    // 열 너비 (기본 20자)
    default int getColumnWidth(int columnIndex) {
        return 20 * 256;
    }
}

// I/F만 만들고 나머지는 상속받아서 처리