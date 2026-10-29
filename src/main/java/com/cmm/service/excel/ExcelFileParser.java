package com.cmm.service.excel;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.util.CellRangeAddress;
import com.cmm.service.excel.ExcelTemplate.*;
import com.cmm.service.excel.ExcelTemplate.Definition;
import com.cmm.service.file.FileVO;
import com.cmm.service.file.impl.FileMngUtil;

/** 업로드 검증·응답 형식·예외 처리는 DOM/SAX가 공유한다. */
public abstract class ExcelFileParser {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final FileMngUtil fileMngUtil;
    private final ExcelTemplate excelTemplate;
    protected ExcelFileParser(FileMngUtil fileMngUtil, ExcelTemplate excelTemplate) {
        this.fileMngUtil = fileMngUtil; this.excelTemplate = excelTemplate;
    }
    protected abstract ReadResult read(InputStream input, Definition template, boolean preview) throws IOException;
    protected String extensionError(String extension) {
        return "xls".equals(extension) || "xlsx".equals(extension) ? null : "EXCEL_EXTENSION_NOT_ALLOWED";
    }
    
    protected final FileVO validate(MultipartFile upload) {
        FileVO file = fileMngUtil.validate(upload);
        String error = extensionError(file.getFileExtsn());
        if (error != null) throw FileMngUtil.bad(error, "files");
        return file;
    }

    protected final FileVO validate(MultipartFile upload, long maxFileSize) {
        FileVO file = fileMngUtil.validate(upload, maxFileSize);
        String error = extensionError(file.getFileExtsn());
        if (error != null) throw FileMngUtil.bad(error, "files");
        return file;
    }
    
    public List<Map<String, Object>> parse(List<MultipartFile> files, String description) {
        return parseFiles(files, null);
    }
    
    public List<Map<String, Object>> parseXml(List<MultipartFile> files, String description, String resource) throws IOException {
        return parseFiles(files, excelTemplate.load(resource));
    }
    
    private List<Map<String, Object>> parseFiles(List<MultipartFile> files, Definition template) {
        fileMngUtil.validateCount(files == null ? 0 : files.size());
        List<FileVO> metadata = new ArrayList<>();
        for (MultipartFile file : files) metadata.add(validate(file));
        List<Map<String, Object>> result = new ArrayList<>();
        for (int index = 0; index < files.size(); index++) {
            FileVO file = metadata.get(index);
            try (InputStream input = new BufferedInputStream(files.get(index).getInputStream())) {
                ReadResult workbook = read(input, template, true);
                Map<String, Object> content = new LinkedHashMap<>();
                
                content.put("sheets", workbook.preview());
                
                if (template != null) content.put("data", workbook.mapped(template));
                
                Map<String, Object> data = new LinkedHashMap<>();
                
                data.put("originalName", file.getOrignlFileNm()); 
                data.put("extension", file.getFileExtsn());
                data.put("fileSize", file.getFileSize()); 
                data.put("fileContent", content); 
                
                result.add(data);
                
            } catch (IOException | RuntimeException exception) {
                logger.warn("Excel upload parsing failed: file={}", file.getOrignlFileNm(), exception);
                throw new FileMngUtil.FileException(400, "EXCEL_PARSE_FAILED", "files", exception);
            }
        }
        return result;
    }
    public Map<String, Object> parseXml(MultipartFile file, String resource) throws IOException {
        return parseMapped(file, excelTemplate.load(resource));
    }
    public Map<String, Object> parseMapped(MultipartFile file, Definition template) throws IOException {
        if (template == null) throw new IllegalArgumentException("엑셀 매핑 양식이 필요합니다.");
        validate(file);
        try (InputStream input = new BufferedInputStream(file.getInputStream())) {
            return read(input, template, false).mapped(template);
        }
    }

    /** 파싱 결과와 XML 매핑에 필요한 공통 데이터/처리. */
    public static final class ReadResult {
        public final List<SheetData> sheets = new ArrayList<>();
    
        public static SheetMapping find(Definition template, String name) {
            if (template != null) for (SheetMapping sheet : template.getSheets()) if (sheet.getName().equals(name)) return sheet;
            return null;
        }
    
        public static final class CellData {
            final Object value;
            final String formatted;
            final boolean error;
            public CellData(Object value, String formatted, boolean error) {
                this.value = value; this.formatted = formatted; this.error = error;
            }
            public String getFormatted() { return formatted; }
        }
    
        public static final class SheetData {
            final String name;
            final boolean preview;
            public final NavigableMap<Integer, NavigableMap<Integer, CellData>> rows = new TreeMap<>();
            public final List<CellRangeAddress> merged = new ArrayList<>();
            private final Set<Long> fixed = new HashSet<>();
            private final Set<Integer> movingColumns = new HashSet<>();
            private int movingStart = Integer.MAX_VALUE;
            public int lastRow;
            
            public SheetData(String name, boolean preview, SheetMapping definition) {
                this.name = name; this.preview = preview;
                boolean moving = false;
                if (definition != null) for (Mapping mapping : definition.getMappings()) {
                    if (mapping instanceof RepeatMapping) {
                        moving = true;
                        for (CellMapping cell : ((RepeatMapping) mapping).getCells()) capture(cell, true);
                    } else if (mapping instanceof CellMapping) capture((CellMapping) mapping, moving);
                }
            }
            private void capture(CellMapping cell, boolean moving) {
                CellRangeAddress range = cell.range(0);
                if (moving) {
                    movingColumns.add(range.getFirstColumn());
                    movingStart = Math.min(movingStart, range.getFirstRow());
                } else fixed.add(key(range.getFirstRow(), range.getFirstColumn()));
            }
            public boolean keep(int row, int column) {
                return preview || fixed.contains(key(row, column)) || (row >= movingStart && movingColumns.contains(column));
            }
            public void put(int row, int column, CellData cell) {
                if (keep(row, column)) rows.computeIfAbsent(row, ignored -> new TreeMap<>()).put(column, cell);
            }
            CellData cell(CellRangeAddress range) {
                Map<Integer, CellData> row = rows.get(range.getFirstRow());
                return row == null ? null : row.get(range.getFirstColumn());
            }
            String formatted(CellRangeAddress range) {
                CellData cell = cell(range);
                return cell == null ? "" : cell.formatted;
            }
            boolean matchesRange(CellRangeAddress expected) {
                boolean found = expected.getNumberOfCells() == 1;
                for (CellRangeAddress actual : merged) if (actual.intersects(expected)) {
                    if (!actual.formatAsString().equals(expected.formatAsString())) return false;
                    found = true;
                }
                return found;
            }
            private static long key(int row, int column) { return ((long) row << 32) | column; }
        }
    
        public List<Map<String, Object>> preview() {
            List<Map<String, Object>> output = new ArrayList<>();
            for (SheetData sheet : sheets) {
                List<Map<String, Object>> rows = new ArrayList<>();
                for (Map.Entry<Integer, NavigableMap<Integer, CellData>> row : sheet.rows.entrySet()) {
                    if (row.getValue().isEmpty()) continue;
                    List<String> cells = new ArrayList<>(Collections.nCopies(row.getValue().lastKey() + 1, ""));
                    boolean hasValue = false;
                    for (Map.Entry<Integer, CellData> cell : row.getValue().entrySet()) {
                        cells.set(cell.getKey(), cell.getValue().formatted);
                        hasValue |= !cell.getValue().formatted.trim().isEmpty();
                    }
                    if (hasValue) {
                        Map<String, Object> result = new LinkedHashMap<>();
                        result.put("rowNumber", row.getKey() + 1); result.put("cells", cells); rows.add(result);
                    }
                }
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("sheetName", sheet.name); result.put("rows", rows); output.add(result);
            }
            return output;
        }
    
        public Map<String, Object> mapped(Definition template) throws IOException {
            Map<String, Object> result = new LinkedHashMap<>();
            for (SheetMapping definition : template.getSheets()) {
                SheetData sheet = null;
                for (SheetData candidate : sheets) if (candidate.name.equals(definition.getName())) { sheet = candidate; break; }
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
                        assign(result, repeat.getItems(), items); offset += count;
                    }
                }
            }
            return result;
        }
    
        private int repeatCount(SheetData sheet, List<Mapping> mappings, int index, int offset) throws IOException {
            List<CellMapping> anchors = new ArrayList<>();
            for (int next = index + 1; next < mappings.size(); next++) {
                Mapping mapping = mappings.get(next);
                if (mapping instanceof RepeatMapping) break;
                if (mapping instanceof CellMapping && ((CellMapping) mapping).getText() != null) anchors.add((CellMapping) mapping);
            }
            if (anchors.isEmpty()) throw new IOException("repeat를 읽으려면 뒤에 고정 text 셀이 필요합니다.");
            int found = -1, lastOffset = Integer.MAX_VALUE;
            for (CellMapping anchor : anchors) lastOffset = Math.min(lastOffset, sheet.lastRow - anchor.range(offset).getLastRow());
            for (int count = 0; count <= lastOffset; count++) {
                boolean matches = true;
                for (CellMapping anchor : anchors) {
                    CellRangeAddress range = anchor.range(offset + count);
                    if (!sheet.matchesRange(range) || !anchor.getText().equals(sheet.formatted(range))) { matches = false; break; }
                }
                if (matches) {
                    if (found >= 0) throw new IOException("반복 영역의 끝이 모호합니다.");
                    found = count;
                }
            }
            if (found < 0) throw new IOException("반복 영역의 끝 문구 또는 병합 범위가 양식과 다릅니다.");
            return found;
        }
        
        private void readCell(SheetData sheet, CellMapping mapping, int offset, Map<String, Object> result) throws IOException {
            CellRangeAddress range = mapping.range(offset);
            if (!sheet.matchesRange(range)) throw new IOException("병합 영역이 양식과 다릅니다: " + range.formatAsString());
            if (mapping.getText() != null) {
                if (!mapping.getText().equals(sheet.formatted(range))) throw new IOException("고정 문구가 양식과 다릅니다: " + range.formatAsString());
            } else {
                CellData cell = sheet.cell(range);
                if (cell != null && cell.error) throw new IOException("읽을 수 없는 셀 값: " + range.formatAsString());
                assign(result, mapping.getProperty(), cell == null ? null : cell.value);
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
            if (current.containsKey(leaf) && !Objects.equals(current.get(leaf), value)) throw new IOException("중복 속성 값이 다릅니다: " + path);
            current.put(leaf, value);
        }
    }
    
}
