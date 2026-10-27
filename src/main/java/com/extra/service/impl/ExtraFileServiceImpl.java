package com.extra.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cmm.service.excel.impl.ExcelSaxFileParser;
import com.cmm.service.file.impl.FileMngUtil;
import com.extra.service.ExtraVO;

/** SAX 파싱·검증 결과를 반환하고, 등록 요청 시 오류 없는 행을 일괄 저장한다. */
@Service
public class ExtraFileServiceImpl {
    private final ExcelSaxFileParser excelFileParser;
    private final ExtraDAO extraDAO;
    private static final String[] DATE_PATTERNS = {
        "yyyy-MM-dd HH:mm:ss.SSS", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm",
        "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy-MM-dd", "yyyy/MM/dd"
    };

    public ExtraFileServiceImpl(ExcelSaxFileParser excelFileParser, ExtraDAO extraDAO) {
        this.excelFileParser = excelFileParser;
        this.extraDAO = extraDAO;
    }

    public Map<String, Object> parseExtraExcel(List<MultipartFile> files) {
        if (files == null || files.size() != 1) throw FileMngUtil.bad("FILE_COUNT_INVALID", "files");
        Map<String, Object> file = excelFileParser.parse(files, null).get(0);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> content = (Map<String, Object>) file.get("fileContent");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> sheets = (List<Map<String, Object>>) content.get("sheets");
        List<ExtraVO> dataList = new ArrayList<>();
        List<ExtraVO> errorList = new ArrayList<>();
        List<ExtraVO> warningList = new ArrayList<>();
        List<Map<String, Object>> cellIssues = new ArrayList<>();
        for (Map<String, Object> sheet : sheets) parseSheet(sheet, dataList, errorList, warningList, cellIssues);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("dataList", dataList);
        result.put("errorList", errorList);
        result.put("warningList", warningList);
        result.put("cellIssues", cellIssues);
        result.put("errorCount", errorList.size());
        result.put("warningCount", warningList.size());
        result.put("totalCount", dataList.size());
        result.put("extension", file.get("extension"));
        result.put("fileSize", file.get("fileSize"));
        result.put("originalName", file.get("originalName"));
        return result;
    }

    /** 클라이언트의 검증 결과를 신뢰하지 않고 파일을 다시 파싱한 후 오류 없는 행을 등록한다. */
    public Map<String, Object> insertExtraExcel(List<MultipartFile> files) {
        Map<String, Object> parsed = parseExtraExcel(files);
        @SuppressWarnings("unchecked")
        List<ExtraVO> dataList = (List<ExtraVO>) parsed.get("dataList");
        @SuppressWarnings("unchecked")
        List<ExtraVO> errorList = (List<ExtraVO>) parsed.get("errorList");
        Set<ExtraVO> errors = new HashSet<>(errorList);
        List<ExtraVO> items = new ArrayList<>();
        for (ExtraVO item : dataList) {
            if (!errors.contains(item)) items.add(item);
        }
        if (items.isEmpty()) throw new InvalidExtraExcelException("등록할 정상 또는 경고 행이 없습니다.");
        int inserted = extraDAO.insertExtraList(items);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("insertedCount", inserted);
        result.put("totalCount", parsed.get("totalCount"));
        result.put("errorCount", parsed.get("errorCount"));
        result.put("warningCount", parsed.get("warningCount"));
        return result;
    }

    public static class InvalidExtraExcelException extends IllegalArgumentException {
        private static final long serialVersionUID = 1L;
        public InvalidExtraExcelException(String message) { super(message); }
    }

    private void parseSheet(Map<String, Object> sheet, List<ExtraVO> dataList,
            List<ExtraVO> errorList, List<ExtraVO> warningList, List<Map<String, Object>> cellIssues) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rows = (List<Map<String, Object>>) sheet.get("rows");
        if (rows.isEmpty()) return;

        for (int index = 1; index < rows.size(); index++) {
            Map<String, Object> row = rows.get(index);
            List<String> values = cells(row);
            ExtraVO item = new ExtraVO();

            // 양식의 고정 열 순서로 매핑한다. 첫 행은 헤더로 건너뛴다.

            boolean hasError = false;
            boolean hasWarning = false;
            
            for (int column = 0; column < 16; column++) {
                String value = column < values.size() ? values.get(column) : "";
                if (value == null || value.trim().isEmpty()) {
                    hasWarning = true;
                    cellIssues.add(cellIssue(dataList.size(), column, "WARNING", value, "비어 있는 셀입니다."));
                    continue;
                }
                try {
                    validateCell(column, value);
                    switch (column) {
                        case 0: item.setBikeId((String) convert(value, String.class)); break;
                        case 1: item.setRentDt((Date) convert(value, Date.class)); break;
                        case 2: item.setRentId((String) convert(value, String.class)); break;
                        case 3: item.setRentNm((String) convert(value, String.class)); break;
                        case 4: item.setRentHold((String) convert(value, String.class)); break;
                        case 5: item.setRtnDt((Date) convert(value, Date.class)); break;
                        case 6: item.setRtnId((String) convert(value, String.class)); break;
                        case 7: item.setRtnNm((String) convert(value, String.class)); break;
                        case 8: item.setRtnHold((String) convert(value, String.class)); break;
                        case 9: item.setUseMin((BigDecimal) convert(value, BigDecimal.class)); break;
                        case 10: item.setUseDst((BigDecimal) convert(value, BigDecimal.class)); break;
                        case 11: item.setBirthYear((String) convert(value, String.class)); break;
                        case 12: item.setSexCd((String) convert(value, String.class)); break;
                        case 13: item.setUsrClsCd((String) convert(value, String.class)); break;
                        case 14: item.setRentStationId((String) convert(value, String.class)); break;
                        case 15: item.setReturnStationId((String) convert(value, String.class)); break;
                        default: break;
                    }
                } catch (IllegalArgumentException exception) {
                    hasError = true;
                    cellIssues.add(cellIssue(dataList.size(), column, "ERROR", value, exception.getMessage()));
                }
            }
            // 전체 행을 유지하고, 오류가 있는 행은 경고 목록에 중복으로 넣지 않는다.
            dataList.add(item);
            if (hasError) errorList.add(item);
            else if (hasWarning) warningList.add(item);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<String> cells(Map<String, Object> row) {
        return (List<String>) row.get("cells");
    }

    /** 행·열 인덱스는 0부터 시작하며, 행은 전체 dataList 기준이다. */
    private static Map<String, Object> cellIssue(int rowIndex, int columnIndex, String status,
            String value, String message) {
        Map<String, Object> issue = new LinkedHashMap<>();
        issue.put("rowIndex", rowIndex);
        issue.put("columnIndex", columnIndex);
        issue.put("status", status);
        issue.put("value", value);
        issue.put("message", message);
        return issue;
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

    private static void validateCell(int column, String value) {
        int maxLength;
        switch (column) {
            case 0: case 2: case 6: case 13: case 14: case 15:
                maxLength = 50; break;
            case 3: case 7:
                maxLength = 200; break;
            case 4: case 8: case 12:
                maxLength = 20; break;
            case 11:
                maxLength = 4; break;
            case 9: case 10:
                BigDecimal number = (BigDecimal) convert(value, BigDecimal.class);
                try { number = number.setScale(2, RoundingMode.UNNECESSARY); }
                catch (ArithmeticException exception) {
                    throw new IllegalArgumentException("소수점 이하 두 자리까지 입력할 수 있습니다.");
                }
                int integerDigits = column == 9 ? 10 : 13;
                if (number.abs().compareTo(BigDecimal.TEN.pow(integerDigits)) >= 0) {
                    throw new IllegalArgumentException("정수부는 " + integerDigits + "자리까지 입력할 수 있습니다.");
                }
                return;
            default:
                return; // 일시의 타입 검사는 convert에서 처리한다.
        }
        if (value.codePointCount(0, value.length()) > maxLength) {
            throw new IllegalArgumentException("최대 " + maxLength + "자까지 입력할 수 있습니다.");
        }
    }

}
