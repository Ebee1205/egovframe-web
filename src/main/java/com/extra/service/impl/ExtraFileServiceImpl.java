package com.extra.service.impl;

import java.math.BigDecimal;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.cmm.service.excel.impl.ExcelSaxFileParser;
import com.cmm.service.file.FileVO;
import com.cmm.service.file.impl.FileMngUtil;
import com.extra.service.ExtraFileService;
import com.extra.service.ExtraVO;

/** 전체 파일을 검증하되 미리보기와 등록용 목록은 각각 상한 안에서만 보관한다. */
@Service
public class ExtraFileServiceImpl implements ExtraFileService {
    private static final int PARSE_BATCH_SIZE = 1000;
    private static final int PREVIEW_LIMIT = 1000;
    private static final int CELL_ISSUE_LIMIT = 1000;
    private static final String[] DATE_PATTERNS = {
        "yyyy-MM-dd HH:mm:ss.SSS", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm",
        "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy-MM-dd", "yyyy/MM/dd"
    };
    private final ExcelSaxFileParser excelFileParser;
    private final ExtraDAO extraDAO;

    public ExtraFileServiceImpl(ExcelSaxFileParser excelFileParser, ExtraDAO extraDAO) {
        this.excelFileParser = excelFileParser;
        this.extraDAO = extraDAO;
    }

    // SAX 원본은 최대 1,000행씩 처리하고, 미리보기는 파일 앞부분 1,000행만 유지한다.
    @Override
    public Map<String, Object> parseExtraExcel(List<MultipartFile> files) {
        if (files == null || files.size() != 1) throw FileMngUtil.bad("FILE_COUNT_INVALID", "files");

        List<ExtraVO> dataList = new ArrayList<>(PREVIEW_LIMIT);
        
        // 기존 전체 목록 수집: 전체 VO를 오류/경고 목록에 중복 보관하지 않는다.
	    List<ExtraVO> errorList = new ArrayList<>();
	    List<ExtraVO> warningList = new ArrayList<>();
	    
        List<Map<String, Object>> cellIssues = new ArrayList<>();
        List<List<String>> batch = new ArrayList<>(PARSE_BATCH_SIZE);
        ParseCounts counts = new ParseCounts();

        FileVO file = excelFileParser.parseRows(files.get(0), new ExcelSaxFileParser.RowConsumer() {
            private boolean header;

            @Override
            public void startSheet(String sheetName) { header = true; }

            @Override
            public void accept(String sheetName, int rowNumber, List<String> values) {
                if (header) {
                    header = false;
                    return;
                }
                batch.add(values);
                if (batch.size() >= PARSE_BATCH_SIZE) parseBatch(batch, dataList, cellIssues, counts);
            }
        });
        parseBatch(batch, dataList, cellIssues, counts);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("dataList", dataList);
		result.put("errorList", errorList);
		result.put("warningList", warningList);
        result.put("cellIssues", cellIssues);
        
        // 기존 목록 크기 기준 집계: 미리보기 상한과 관계없이 파일 전체를 집계한다.
        // result.put("errorCount", errorList.size());
        // result.put("warningCount", warningList.size());
        // result.put("totalCount", dataList.size());
        result.put("errorCount", counts.errorCount);
        result.put("warningCount", counts.warningCount);
        result.put("totalCount", counts.totalCount);
        
        result.put("extension", file.getFileExtsn());
        result.put("fileSize", file.getFileSize());
        result.put("originalName", file.getOrignlFileNm());
        return result;
    }

    // 1,000행씩 검증 후 정상·경고 행만 등록한다. 실패하면 이전 배치까지 롤백한다.
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> insertExtraExcel(List<MultipartFile> files) {
        if (files == null || files.size() != 1) throw FileMngUtil.bad("FILE_COUNT_INVALID", "files");
        List<List<String>> batch = new ArrayList<>(PARSE_BATCH_SIZE);
        ParseCounts counts = new ParseCounts();
        excelFileParser.parseRows(files.get(0), new ExcelSaxFileParser.RowConsumer() {
            private boolean header;

            @Override
            public void startSheet(String sheetName) { header = true; }

            @Override
            public void accept(String sheetName, int rowNumber, List<String> values) {
                if (header) {
                    header = false;
                    return;
                }
                batch.add(values);
                if (batch.size() >= PARSE_BATCH_SIZE) parseBatch(batch, null, null, counts);
            }
        });
        parseBatch(batch, null, null, counts);
        if (counts.insertedCount == 0) throw FileMngUtil.bad("EXTRA_EXCEL_NO_INSERTABLE_ROWS", "files");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("insertedCount", counts.insertedCount);
        result.put("totalCount", counts.totalCount);
        result.put("errorCount", counts.errorCount);
        result.put("warningCount", counts.warningCount);
        return result;
    }

    // dataList가 null이면 등록 모드: 미리보기/셀 오류 목록을 만들지 않는다.
    private void parseBatch(List<List<String>> batch, List<ExtraVO> dataList,
            List<Map<String, Object>> cellIssues, ParseCounts counts) {
        if (batch.isEmpty()) return;
        boolean inserting = dataList == null;
        List<ExtraVO> items = inserting ? new ArrayList<>(batch.size()) : null;
        for (List<String> values : batch) {
            int rowIndex = counts.totalCount++;
            boolean preview = !inserting && dataList.size() < PREVIEW_LIMIT;
            ExtraVO item = inserting || preview ? new ExtraVO() : null;
            boolean hasError = false;
            boolean hasWarning = false;
            for (int column = 0; column < 16; column++) {
                String value = column < values.size() ? values.get(column) : "";
                boolean empty = value == null || value.trim().isEmpty();
                String message = empty ? "비어 있는 셀입니다." : checkCell(column, value);
                if (message != null) {
                    if (empty) hasWarning = true;
                    else hasError = true;
                    // rowIndex는 기존 화면과 같은 0부터 시작하는 데이터 행 위치다.
                    if (preview && cellIssues.size() < CELL_ISSUE_LIMIT) {
                        cellIssues.add(cellIssue(rowIndex, column, empty ? "WARNING" : "ERROR", value, message));
                    }
                    continue;
                }
                if (item == null) continue;
                switch (column) {
                    case 0: item.setBikeId(value); break;
                    case 1: item.setRentDt(parseDate(value)); break;
                    case 2: item.setRentId(value); break;
                    case 3: item.setRentNm(value); break;
                    case 4: item.setRentHold(value); break;
                    case 5: item.setRtnDt(parseDate(value)); break;
                    case 6: item.setRtnId(value); break;
                    case 7: item.setRtnNm(value); break;
                    case 8: item.setRtnHold(value); break;
                    case 9: item.setUseMin(parseNumber(value)); break;
                    case 10: item.setUseDst(parseNumber(value)); break;
                    case 11: item.setBirthYear(value); break;
                    case 12: item.setSexCd(value); break;
                    case 13: item.setUsrClsCd(value); break;
                    case 14: item.setRentStationId(value); break;
                    case 15: item.setReturnStationId(value); break;
                    default: throw new IllegalArgumentException("지원하지 않는 컬럼입니다.");
                }
            }
            if (hasError) counts.errorCount++;
            else if (hasWarning) counts.warningCount++;
            if (preview) dataList.add(item);
            if (inserting && !hasError) items.add(item);
        }
        batch.clear();
        if (inserting && !items.isEmpty()) counts.insertedCount += extraDAO.insertExtraList(items);
    }

    private static Map<String, Object> cellIssue(int rowIndex, int columnIndex, String status, String value, String message) {
        Map<String, Object> issue = new LinkedHashMap<>();
        issue.put("rowIndex", rowIndex);
        issue.put("columnIndex", columnIndex);
        issue.put("status", status);
        issue.put("value", value);
        issue.put("message", message);
        return issue;
    }

    // 정상은 null, 입력 오류는 안내 메시지를 반환한다. 검증 실패를 예외로 던지지 않는다.
    private static String checkCell(int column, String value) {
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
            case 1: case 5:
                return parseDate(value) == null ? "날짜 형식으로 읽을 수 없습니다. 예: 2026-10-27 16:00:00" : null;
            case 9: case 10:
                BigDecimal number = parseNumber(value);
                if (number == null) return "숫자 형식으로 읽을 수 없습니다.";
                if (number.stripTrailingZeros().scale() > 2) return "소수점 이하 두 자리까지 입력할 수 있습니다.";
                int integerDigits = column == 9 ? 10 : 13;
                if (number.abs().compareTo(BigDecimal.TEN.pow(integerDigits)) >= 0) {
                    return "정수부는 " + integerDigits + "자리까지 입력할 수 있습니다.";
                }
                return null;
            default:
                throw new IllegalArgumentException("지원하지 않는 컬럼입니다.");
        }
        return value.codePointCount(0, value.length()) > maxLength ? "최대 " + maxLength + "자까지 입력할 수 있습니다." : null;
    }

    private static BigDecimal parseNumber(String value) {
        String text = value.trim();
        if (text.matches("[+-]?\\d{1,3}(,\\d{3})+(\\.\\d+)?")) text = text.replace(",", "");
        try { return new BigDecimal(text); }
        catch (NumberFormatException exception) { return null; }
    }

    private static Date parseDate(String value) {
        String text = value.trim();
        for (String pattern : DATE_PATTERNS) {
            SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.KOREAN);
            format.setLenient(false);
            ParsePosition position = new ParsePosition(0);
            Date date = format.parse(text, position);
            if (date != null && position.getIndex() == text.length()) return date;
        }
        return null;
    }
    // 요청마다 따로 생성한다. 오류·경고 건수는 행 단위다.
    private static class ParseCounts {
        private int totalCount;
        private int errorCount;
        private int warningCount;
        private int insertedCount;
    }
}
