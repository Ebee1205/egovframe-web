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

import java.util.function.Consumer;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.cmm.service.excel.impl.ExcelSaxFileParser;
import com.cmm.service.file.impl.FileMngUtil;
import com.extra.service.ExtraVO;
import com.extra.service.ExtraFileService;
import com.cmm.service.file.FileVO;

/** SAX 파싱·검증 결과를 반환하고, 등록 요청 시 오류 없는 행을 일괄 저장한다. */
@Service
public class ExtraFileServiceImpl implements ExtraFileService {
    private static final int PARSE_BATCH_SIZE = 1000;
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

    // 엑셀 파일 파싱
    // SAX 원본 행은 최대 100행만 모아서 처리하고 비운다.
    // 배치 단위로 validation check > 정상/error/warning 구분해서 json에 add하기
    @Override
    public Map<String, Object> parseExtraExcel(List<MultipartFile> files) {
        
    	if (files == null || files.size() != 1) throw FileMngUtil.bad("FILE_COUNT_INVALID", "files");

        List<ExtraVO> dataList = new ArrayList<>();
        List<ExtraVO> errorList = new ArrayList<>();
        List<ExtraVO> warningList = new ArrayList<>();
        List<Map<String, Object>> cellIssues = new ArrayList<>();
        List<List<String>> batch = new ArrayList<>(PARSE_BATCH_SIZE);

        FileVO file = excelFileParser.parseRows(files.get(0), new ExcelSaxFileParser.RowConsumer() {
            private boolean header;

            @Override
            public void startSheet(String sheetName) {
                header = true;
            }

            @Override
            public void accept(String sheetName, int rowNumber, List<String> values) {
                if (header) {
                    header = false;
                    return;
                }
                
                batch.add(values);
                
                if (batch.size() >= PARSE_BATCH_SIZE) {
                    parseBatch(batch, dataList, errorList, warningList, cellIssues);
                }
            }
        });
        
        // 남은 행 처리... 
        parseBatch(batch, dataList, errorList, warningList, cellIssues);
                
        Map<String, Object> result = new LinkedHashMap<>();
        
        result.put("dataList", dataList);
        result.put("errorList", errorList);
        result.put("warningList", warningList);
        result.put("cellIssues", cellIssues);
        
        result.put("errorCount", errorList.size());
        result.put("warningCount", warningList.size());
        result.put("totalCount", dataList.size());
        
        result.put("extension", file.getFileExtsn());
        result.put("fileSize", file.getFileSize());
        result.put("originalName", file.getOrignlFileNm());

        return result;
    }

    // 100행씩 파싱·검증 후 바로 등록/ 실패시 먼저 실행된 배치도 롤백
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> insertExtraExcel(List<MultipartFile> files) {
        
    	if (files == null || files.size() != 1) throw FileMngUtil.bad("FILE_COUNT_INVALID", "files");

        List<List<String>> batch = new ArrayList<>(PARSE_BATCH_SIZE);
        List<ExtraVO> dataList = new ArrayList<>(PARSE_BATCH_SIZE);
        List<ExtraVO> errorList = new ArrayList<>();
        List<ExtraVO> warningList = new ArrayList<>();
        List<Map<String, Object>> cellIssues = new ArrayList<>();
        int[] counts = new int[4]; // 등록, 전체, 오류, 경고 행 수

        Consumer<List<List<String>>> insertBatch = rows -> {
            if (rows.isEmpty()) return;
            
            parseBatch(rows, dataList, errorList, warningList, cellIssues);
            
            counts[1] += dataList.size();
            counts[2] += errorList.size();
            counts[3] += warningList.size();
            dataList.removeAll(errorList);
            
            if (!dataList.isEmpty()) counts[0] += extraDAO.insertExtraList(dataList);
            
            // ㅋㅋ 왜 굳이,, 걍 첨부터 만들지 말지
            dataList.clear();
            errorList.clear();
            warningList.clear();
            cellIssues.clear();
        };

        excelFileParser.parseRows(files.get(0), new ExcelSaxFileParser.RowConsumer() {
            private boolean header;

            @Override
            public void startSheet(String sheetName) {
                header = true;
            }

            @Override
            public void accept(String sheetName, int rowNumber, List<String> values) {
                if (header) {
                    header = false;
                    return;
                }
                batch.add(values);
                if (batch.size() >= PARSE_BATCH_SIZE) insertBatch.accept(batch);
            }
        });
        
        insertBatch.accept(batch); // 남은 데이터 insert
        
        if (counts[0] == 0) throw FileMngUtil.bad("EXTRA_EXCEL_NO_INSERTABLE_ROWS", "files");

        Map<String, Object> result = new LinkedHashMap<>();
        
        result.put("insertedCount", counts[0]);
        result.put("totalCount", counts[1]);
        result.put("errorCount", counts[2]);
        result.put("warningCount", counts[3]);
        
        return result;
    }

    private void parseBatch(List<List<String>> batch, List<ExtraVO> dataList,
            List<ExtraVO> errorList, List<ExtraVO> warningList, List<Map<String, Object>> cellIssues) {
        for (List<String> values : batch) {
            ExtraVO item = new ExtraVO();

            // 양식의 고정 열 순서로 현재 행만 변환·검증한다.

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
                } catch (FileMngUtil.FileException exception) {
                    hasError = true;
                    cellIssues.add(cellIssue(dataList.size(), column, "ERROR", value, exception.getMessage()));
                }
            }
            // 전체 행을 유지하고, 오류가 있는 행은 경고 목록에 중복으로 넣지 않는다.
            dataList.add(item);
            if (hasError) errorList.add(item);
            else if (hasWarning) warningList.add(item);
        }
        batch.clear();
    }

    // 경고/오류 셀 양식
    private static Map<String, Object> cellIssue(int rowIndex, int columnIndex, String status, String value, String message) {
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
            catch (NumberFormatException exception) { throw invalidCell("숫자 형식으로 읽을 수 없습니다."); }
        }
        if (type == Date.class) {
            for (String pattern : DATE_PATTERNS) {
                SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.KOREAN);
                format.setLenient(false);
                ParsePosition position = new ParsePosition(0);
                Date date = format.parse(text, position);
                if (date != null && position.getIndex() == text.length()) return date;
            }
            throw invalidCell("날짜 형식으로 읽을 수 없습니다. 예: 2026-10-27 16:00:00");
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
                    throw invalidCell("소수점 이하 두 자리까지 입력할 수 있습니다.");
                }
                int integerDigits = column == 9 ? 10 : 13;
                if (number.abs().compareTo(BigDecimal.TEN.pow(integerDigits)) >= 0) {
                    throw invalidCell("정수부는 " + integerDigits + "자리까지 입력할 수 있습니다.");
                }
                return;
            default:
                return; // 일시의 타입 검사는 convert에서 처리한다.
        }
        if (value.codePointCount(0, value.length()) > maxLength) {
            throw invalidCell("최대 " + maxLength + "자까지 입력할 수 있습니다.");
        }
    }

    private static FileMngUtil.FileException invalidCell(String message) {
        return new FileMngUtil.FileException(400, "EXTRA_EXCEL_INVALID", "files", message, null);
    }
}
