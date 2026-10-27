package com.extra.service.impl;

import java.math.BigDecimal;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cmm.service.excel.impl.ExcelSaxFileParser;
import com.cmm.service.file.impl.FileMngUtil;
import com.extra.service.ExtraVO;

/** SAX 결과를 저장용 VO로 변환한다. DB 조회, 등록 및 업무 검증은 수행하지 않는다. */
@Service
public class ExtraFileServiceImpl {
    private final ExcelSaxFileParser excelFileParser;
    private static final Map<String, String> HEADER_PROPERTIES = headerProperties();
    private static final String[] DATE_PATTERNS = {
        "yyyy-MM-dd HH:mm:ss.SSS", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm",
        "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy-MM-dd", "yyyy/MM/dd"
    };

    public ExtraFileServiceImpl(ExcelSaxFileParser excelFileParser) {
        this.excelFileParser = excelFileParser;
    }

    public List<Map<String, Object>> parseExtraExcel(List<MultipartFile> files) {
        if (files == null || files.size() != 1) throw FileMngUtil.bad("FILE_COUNT_INVALID", "files");
        List<Map<String, Object>> parsed = excelFileParser.parse(files, null);
        for (Map<String, Object> file : parsed) {
            @SuppressWarnings("unchecked")
            Map<String, Object> content = (Map<String, Object>) file.get("fileContent");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> sheets = (List<Map<String, Object>>) content.get("sheets");
            List<ExtraVO> data = new ArrayList<>();
            for (Map<String, Object> sheet : sheets) parseSheet(sheet, data);
            // 기존 미리보기 응답은 유지하고, DAO에 전달할 수 있는 VO 목록을 추가한다.
            content.put("data", data);
        }
        return parsed;
    }

    private void parseSheet(Map<String, Object> sheet, List<ExtraVO> data) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rows = (List<Map<String, Object>>) sheet.get("rows");
        if (rows.isEmpty()) return;
        List<String> headers = cells(rows.get(0));
        Map<Integer, String> properties = new LinkedHashMap<>();
        for (int column = 0; column < headers.size(); column++) {
            String property = HEADER_PROPERTIES.get(normalize(headers.get(column)));
            if (property != null) {
                if (properties.containsValue(property)) {
                    throw invalid(sheet, rows.get(0), column, "같은 항목의 헤더가 중복되었습니다.");
                }
                properties.put(column, property);
            }
        }
        if (!properties.containsValue("bikeId")) {
            throw invalid(sheet, rows.get(0), 0, "첫 행에서 자전거번호 또는 BIKE_ID 헤더를 찾을 수 없습니다.");
        }
        List<Map<String, Object>> previewRows = new ArrayList<>();
        for (int index = 1; index < rows.size(); index++) {
            Map<String, Object> row = rows.get(index);
            List<String> values = cells(row);
            ExtraVO item = new ExtraVO();
            BeanWrapper bean = new BeanWrapperImpl(item);
            for (Map.Entry<Integer, String> mapping : properties.entrySet()) {
                int column = mapping.getKey();
                String value = column < values.size() ? values.get(column) : "";
                try {
                    bean.setPropertyValue(mapping.getValue(), convert(value, bean.getPropertyType(mapping.getValue())));
                } catch (IllegalArgumentException exception) {
                    throw invalid(sheet, row, column, headers.get(column) + ": " + exception.getMessage());
                }
            }
            data.add(item);
            previewRows.add(row);
        }
        // 헤더 행은 데이터 건수 및 미리보기 본문에서 제외한다.
        sheet.put("rows", previewRows);
    }

    @SuppressWarnings("unchecked")
    private static List<String> cells(Map<String, Object> row) {
        return (List<String>) row.get("cells");
    }

    private static Object convert(String value, Class<?> type) {
        if (value == null || value.trim().isEmpty()) return null;
        // ID와 코드는 숫자로 바꾸지 않으며, 원본 문자열을 유지한다.
        if (type == String.class) return value;
        String text = value.trim();
        if (type == BigDecimal.class) {
            if (text.matches("[+-]?\\d{1,3}(,\\d{3})+(\\.\\d+)?")) text = text.replace(",", "");
            try { return new BigDecimal(text); }
            catch (NumberFormatException exception) { throw new IllegalArgumentException("숫자 형식으로 읽을 수 없습니다."); }
        }
        if (type == Date.class) {
            for (String pattern : DATE_PATTERNS) {
                SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.KOREAN);
                format.setLenient(false);
                ParsePosition position = new ParsePosition(0);
                Date date = format.parse(text, position);
                if (date != null && position.getIndex() == text.length()) return date;
            }
            throw new IllegalArgumentException("날짜 형식으로 읽을 수 없습니다. 예: 2026-10-27 16:00:00");
        }
        throw new IllegalArgumentException("지원하지 않는 컬럼 타입입니다.");
    }

    private static InvalidExtraExcelException invalid(Map<String, Object> sheet, Map<String, Object> row,
            int column, String reason) {
        return new InvalidExtraExcelException(sheet.get("sheetName") + " 시트 / " + row.get("rowNumber")
                + "행 / " + (column + 1) + "열: " + reason);
    }

    public static class InvalidExtraExcelException extends IllegalArgumentException {
        private static final long serialVersionUID = 1L;
        public InvalidExtraExcelException(String message) { super(message); }
    }

    private static String normalize(String header) {
        return header.replace("\uFEFF", "").replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
    }

    private static Map<String, String> headerProperties() {
        Map<String, String> headers = new HashMap<>();
        alias(headers, "bikeId", "BIKE_ID", "자전거ID", "자전거번호");
        alias(headers, "rentDt", "RENT_DT", "대여일시");
        alias(headers, "rentId", "RENT_ID", "대여소번호", "대여 대여소번호");
        alias(headers, "rentNm", "RENT_NM", "대여소명", "대여 대여소명");
        alias(headers, "rentHold", "RENT_HOLD", "대여거치대");
        alias(headers, "rtnDt", "RTN_DT", "반납일시");
        alias(headers, "rtnId", "RTN_ID", "반납대여소번호", "반납대여소대여소번호");
        alias(headers, "rtnNm", "RTN_NM", "반납대여소명");
        alias(headers, "rtnHold", "RTN_HOLD", "반납거치대");
        alias(headers, "useMin", "USE_MIN", "사용(분)", "사용시간(분)", "이용시간(분)");
        alias(headers, "useDst", "USE_DST", "이용거리(M)");
        alias(headers, "usrClsCd", "USR_CLS_CD", "사용자종류", "이용자종류");
        alias(headers, "sexCd", "SEX_CD", "성별");
        alias(headers, "birthYear", "BIRTH_YEAR", "생년");
        alias(headers, "rentStationId", "RENT_STATION_ID", "대여대여소ID");
        alias(headers, "returnStationId", "RETURN_STATION_ID", "반납대여소ID");
        alias(headers, "bikeSeCd", "BIKE_SE_CD", "자전거구분");
        return headers;
    }

    private static void alias(Map<String, String> headers, String property, String... names) {
        for (String name : names) headers.put(normalize(name), property);
    }
}
