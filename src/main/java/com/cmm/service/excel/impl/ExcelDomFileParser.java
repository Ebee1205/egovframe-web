package com.cmm.service.excel.impl;

import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import com.cmm.service.excel.ExcelTemplate;
import com.cmm.service.excel.ExcelTemplate.*;
import com.cmm.service.file.impl.FileMngUtil;
import com.cmm.service.excel.ExcelFileParser;
import com.cmm.service.excel.ExcelFileParser.ReadResult.*;

/** 워크북 객체로 XLS/XLSX를 읽는 기본 구현. */
@Component
@Primary
public class ExcelDomFileParser extends ExcelFileParser {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ROOT);
    public ExcelDomFileParser(FileMngUtil fileMngUtil) { this(fileMngUtil, new ExcelTemplate()); }
    @Autowired
    public ExcelDomFileParser(FileMngUtil fileMngUtil, ExcelTemplate template) { super(fileMngUtil, template); }

    @Override
    protected ReadResult read(InputStream input, Definition template, boolean preview) throws IOException {
        ReadResult result = new ReadResult();
        
        try (Workbook workbook = WorkbookFactory.create(input)) {
            DataFormatter formatter = new DataFormatter(Locale.KOREAN);
            formatter.setUseCachedValuesForFormulaCells(true);
            
            for (Sheet sheet : workbook) {
                SheetMapping definition = ReadResult.find(template, sheet.getSheetName());
                
                if (!preview && definition == null) continue;
                SheetData target = new SheetData(sheet.getSheetName(), preview, definition);
                target.lastRow = sheet.getLastRowNum(); target.merged.addAll(sheet.getMergedRegions());
                
                for (Row row : sheet) for (Cell cell : row) {
                    if (!target.keep(row.getRowNum(), cell.getColumnIndex())) continue;
                    
                    CellType type = cell.getCellType() == CellType.FORMULA ? cell.getCachedFormulaResultType() : cell.getCellType();
                    boolean date = type == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell);
                    Object value = null;
                    
                    switch (type) {
	                    case STRING: 
	                    	value = cell.getStringCellValue(); 
	                    	break;
	                    	
	                    case NUMERIC:
	                    	value = date ? cell.getDateCellValue() : cell.getNumericCellValue(); 
	                    	break;
	                    	
	                    case BOOLEAN: 
	                    	value = cell.getBooleanCellValue(); 
	                    	break;
	                    	
	                    default: 
	                    	break;
                    }
                    
                    String formatted = date ? cell.getLocalDateTimeCellValue().format(DATE_FORMAT) : formatter.formatCellValue(cell);
                    target.put(row.getRowNum(), cell.getColumnIndex(), new CellData(value, formatted,
                            type != CellType.BLANK && type != CellType.STRING && type != CellType.NUMERIC && type != CellType.BOOLEAN));
                }
                result.sheets.add(target);
            }
            
        }
        return result;
    }
}
