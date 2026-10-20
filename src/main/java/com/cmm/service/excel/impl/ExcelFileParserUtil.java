package com.cmm.service.excel.impl;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.cmm.service.file.FileVO;
import com.cmm.service.file.impl.FileMngUtil;
import com.cmm.service.excel.ExcelFileParser;
import com.cmm.service.excel.ExcelTemplate;
import com.cmm.service.excel.ExcelTemplate.*;
import org.apache.poi.ss.util.CellRangeAddress;

/** 업로드 스트림을 파싱하며 파일 저장소 및 파일 DB를 사용하지 않는다. */
@Component
public class ExcelFileParserUtil implements ExcelFileParser {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ExcelFileParserUtil.class);
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ROOT);
    private final FileMngUtil fileMngUtil;
    private final ExcelTemplate excelTemplate;

    public ExcelFileParserUtil(FileMngUtil fileMngUtil) {
        this(fileMngUtil, new ExcelTemplate());
    }

    @org.springframework.beans.factory.annotation.Autowired
    public ExcelFileParserUtil(FileMngUtil fileMngUtil, ExcelTemplate excelTemplate) {
        this.fileMngUtil = fileMngUtil;
        this.excelTemplate = excelTemplate;
    }

    // 엑셀 파일 범용 parser
    public List<Map<String, Object>> parse(List<MultipartFile> files, String description) {
        return parseFiles(files, description, null);
    }

    /** 기존 셀 미리보기 응답을 유지하며 fileContent.data에 XML 매핑 결과를 추가한다. */
    public List<Map<String, Object>> parseXml(List<MultipartFile> files, String description, String classpathXml) throws IOException {
        return parseFiles(files, description, excelTemplate.load(classpathXml));
    }

    private List<Map<String, Object>> parseFiles(List<MultipartFile> files, String description, Definition template) {
    	
        fileMngUtil.validateCount(files == null ? 0 : files.size());
        List<FileVO> metadata = new ArrayList<>();
        for (MultipartFile upload : files) {
            FileVO file = fileMngUtil.validate(upload);
            if (!"xls".equals(file.getFileExtsn()) && !"xlsx".equals(file.getFileExtsn())) {
                throw FileMngUtil.bad("EXCEL_EXTENSION_NOT_ALLOWED", "files");
            }
            metadata.add(file);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            FileVO file = metadata.get(i);
            try (InputStream input = new BufferedInputStream(files.get(i).getInputStream());
                    Workbook workbook = WorkbookFactory.create(input)) {
                DataFormatter formatter = new DataFormatter(Locale.KOREAN);
                
                // 수식은 실행하지 않고 파일에 저장된 계산 결과를 읽는다.
                formatter.setUseCachedValuesForFormulaCells(true);
                List<Map<String, Object>> sheets = new ArrayList<>();
                
                for (Sheet sheet : workbook) {
                    List<Map<String, Object>> rows = new ArrayList<>();
                    for (Row row : sheet) {
                        List<String> cells = new ArrayList<>();
                        boolean hasValue = false;
                        for (int column = 0; column < row.getLastCellNum(); column++) {
                            Cell cell = row.getCell(column, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                            CellType type = cell == null ? CellType.BLANK : cell.getCellType();
                            if (type == CellType.FORMULA) { type = cell.getCachedFormulaResultType(); }
                            String value = cell != null && type == CellType.NUMERIC
                                    && DateUtil.isCellDateFormatted(cell)
                                    ? cell.getLocalDateTimeCellValue().format(DATE_TIME_FORMAT)
                                    : formatter.formatCellValue(cell);
                            cells.add(value);
                            hasValue |= !value.trim().isEmpty();
                        }
                        if (hasValue) {
                            Map<String, Object> rowData = new LinkedHashMap<>();
                            rowData.put("rowNumber", row.getRowNum() + 1);
                            rowData.put("cells", cells);
                            rows.add(rowData);
                        }
                    }
                    Map<String, Object> sheetData = new LinkedHashMap<>();
                    sheetData.put("sheetName", sheet.getSheetName());
                    sheetData.put("rows", rows);
                    sheets.add(sheetData);
                }
                
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("originalName", file.getOrignlFileNm());
                data.put("extension", file.getFileExtsn());
                data.put("fileSize", file.getFileSize());
                
                Map<String, Object> fileContent = new LinkedHashMap<>();
                fileContent.put("sheets", sheets);
                if (template != null) fileContent.put("data", readMapped(workbook, template));
                data.put("fileContent", fileContent);
                result.add(data);
                
            } catch (IOException | RuntimeException exception) {
                LOGGER.warn("Excel upload parsing failed: file={}", file.getOrignlFileNm(), exception);
                throw new FileMngUtil.FileException(400, "EXCEL_PARSE_FAILED", "files", exception);
            }
        }
        return result;
    }
    
    @Override
    public Map<String, Object> parseXml(MultipartFile file, String classpathXml) throws IOException {
        return parseMapped(file, excelTemplate.load(classpathXml));
    }

    @Override
    public Map<String, Object> parseMapped(MultipartFile file, Definition template) throws IOException {
        if (template == null) throw new IllegalArgumentException("엑셀 매핑 양식이 필요합니다.");
        validateExcel(file);
        try (InputStream input = new BufferedInputStream(file.getInputStream()); Workbook workbook = WorkbookFactory.create(input)) {
            return readMapped(workbook, template);
        }
    }

    private void validateExcel(MultipartFile upload) {
        FileVO file = fileMngUtil.validate(upload);
        if (!"xls".equals(file.getFileExtsn()) && !"xlsx".equals(file.getFileExtsn())) {
            throw FileMngUtil.bad("EXCEL_EXTENSION_NOT_ALLOWED", "files");
        }
    }

    private Map<String, Object> readMapped(Workbook workbook, Definition template) throws IOException {
        Map<String, Object> result = new LinkedHashMap<>();
        for (SheetMapping definition : template.getSheets()) {
            Sheet sheet = workbook.getSheet(definition.getName());
            if (sheet == null) throw new IOException("양식의 시트를 찾을 수 없습니다: " + definition.getName());
            int offset = 0;
            List<Mapping> mappings = definition.getMappings();
            for (int index = 0; index < mappings.size(); index++) {
                Mapping mapping = mappings.get(index);
                if (mapping instanceof CellMapping) readCell(sheet, (CellMapping) mapping, offset, result);
                else if (mapping instanceof RepeatMapping) {
                    RepeatMapping repeat = (RepeatMapping) mapping;
                    int count = repeatCount(sheet, mappings, index, offset);
                    List<Map<String, Object>> items = new ArrayList<>();
                    for (int item = 0; item < count; item++) {
                        Map<String, Object> value = new LinkedHashMap<>();
                        for (CellMapping cell : repeat.getCells()) readCell(sheet, cell, offset + item, value);
                        items.add(value);
                    }
                    assign(result, repeat.getItems(), items);
                    offset += count;
                }
            }
        }
        return result;
    }

    /** 반복 끝은 후속 고정 문구와 병합 범위로 확인한다. 경계가 모호한 파일은 거부한다. */
    private int repeatCount(Sheet sheet, List<Mapping> mappings, int index, int offset) throws IOException {
        List<CellMapping> anchors = new ArrayList<>();
        for (int next = index + 1; next < mappings.size(); next++) {
            Mapping mapping = mappings.get(next);
            if (mapping instanceof RepeatMapping) break;
            if (mapping instanceof CellMapping && ((CellMapping) mapping).getText() != null) anchors.add((CellMapping) mapping);
        }
        if (anchors.isEmpty()) throw new IOException("repeat를 읽으려면 뒤에 고정 text 셀이 필요합니다.");
        int found = -1;
        int lastOffset = sheet.getLastRowNum() - anchors.get(0).range(offset).getFirstRow();
        for (int count = 0; count <= lastOffset; count++) {
            boolean matches = true;
            for (CellMapping anchor : anchors) {
                CellRangeAddress range = anchor.range(offset + count);
                if (!matchesRange(sheet, range) || !anchor.getText().equals(formatted(sheet, range))) { matches = false; break; }
            }
            if (matches) {
                if (found >= 0) throw new IOException("반복 영역의 끝이 모호합니다.");
                found = count;
            }
        }
        if (found < 0) throw new IOException("반복 영역의 끝 문구 또는 병합 범위가 양식과 다릅니다.");
        return found;
    }

    private void readCell(Sheet sheet, CellMapping mapping, int offset, Map<String, Object> result) throws IOException {
        CellRangeAddress range = mapping.range(offset);
        if (!matchesRange(sheet, range)) throw new IOException("병합 영역이 양식과 다릅니다: " + range.formatAsString());
        if (mapping.getText() != null) {
            if (!mapping.getText().equals(formatted(sheet, range))) throw new IOException("고정 문구가 양식과 다릅니다: " + range.formatAsString());
        } else {
            Row row = sheet.getRow(range.getFirstRow());
            Cell cell = row == null ? null : row.getCell(range.getFirstColumn());
            assign(result, mapping.getProperty(), value(cell));
        }
    }

    private boolean matchesRange(Sheet sheet, CellRangeAddress expected) {
        boolean found = expected.getNumberOfCells() == 1;
        for (CellRangeAddress actual : sheet.getMergedRegions()) {
            if (actual.intersects(expected)) {
                if (!actual.formatAsString().equals(expected.formatAsString())) return false;
                found = true;
            }
        }
        return found;
    }
    private String formatted(Sheet sheet, CellRangeAddress range) {
        Row row = sheet.getRow(range.getFirstRow());
        return formatter().formatCellValue(row == null ? null : row.getCell(range.getFirstColumn()));
    }
    private DataFormatter formatter() {
        DataFormatter formatter = new DataFormatter(Locale.KOREAN);
        formatter.setUseCachedValuesForFormulaCells(true);
        return formatter;
    }
    private Object value(Cell cell) throws IOException {
        if (cell == null) return null;
        CellType type = cell.getCellType() == CellType.FORMULA ? cell.getCachedFormulaResultType() : cell.getCellType();
        switch (type) {
        case BLANK: return null;
        case STRING: return cell.getStringCellValue();
        case BOOLEAN: return cell.getBooleanCellValue();
        case NUMERIC: return DateUtil.isCellDateFormatted(cell) ? cell.getDateCellValue() : cell.getNumericCellValue();
        default: throw new IOException("읽을 수 없는 셀 값: " + cell.getAddress());
        }
    }

    @SuppressWarnings("unchecked")
    private void assign(Map<String, Object> target, String path, Object value) throws IOException {
        String[] parts = path.split("\\.");
        Map<String, Object> current = target;
        for (int i = 0; i < parts.length - 1; i++) {
            if (!current.containsKey(parts[i])) current.put(parts[i], new LinkedHashMap<String, Object>());
            Object nested = current.get(parts[i]);
            if (!(nested instanceof Map<?, ?>)) throw new IOException("속성 경로가 충돌합니다: " + path);
            current = (Map<String, Object>) nested;
        }
        String leaf = parts[parts.length - 1];
        if (current.containsKey(leaf) && !java.util.Objects.equals(current.get(leaf), value)) throw new IOException("중복 속성 값이 다릅니다: " + path);
        current.put(leaf, value);
    }
    
}
