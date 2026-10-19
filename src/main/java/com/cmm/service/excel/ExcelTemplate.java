package com.cmm.service.excel;

import java.util.List;

public interface ExcelTemplate<T> {

    // 시트 이름
    String getSheetName();

    // 컬럼 헤더 (순서 유지)
    List<String> getHeaders();

    // 엑셀 행 → Java 객체
    T parseRow(List<String> values);

    // Java 객체 → 엑셀 행
    List<?> writeRow(T data);
}