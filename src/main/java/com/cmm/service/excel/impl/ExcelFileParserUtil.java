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

/** 업로드 스트림을 파싱하며 파일 저장소 및 파일 DB를 사용하지 않는다. */
@Component
public class ExcelFileParserUtil {
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ROOT);
    private final FileMngUtil fileMngUtil;

    public ExcelFileParserUtil(FileMngUtil fileMngUtil) {
        this.fileMngUtil = fileMngUtil;
    }

    // 엑셀 파일 범용 parser
    public List<Map<String, Object>> parse(List<MultipartFile> files, String description) {
    	
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
                data.put("fileContent", fileContent);
                result.add(data);
                
            } catch (IOException | RuntimeException exception) {
                throw FileMngUtil.bad("EXCEL_PARSE_FAILED", "files");
            }
        }
        return result;
    }
    
    //
    
}
