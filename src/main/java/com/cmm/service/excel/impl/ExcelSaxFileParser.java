package com.cmm.service.excel.impl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStrings;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.cmm.service.excel.ExcelFileParser;
import com.cmm.service.excel.ExcelFileParser.ReadResult.CellData;
import com.cmm.service.excel.ExcelFileParser.ReadResult.SheetData;
import com.cmm.service.excel.ExcelTemplate;
import com.cmm.service.excel.ExcelTemplate.Definition;
import com.cmm.service.excel.ExcelTemplate.SheetMapping;
import com.cmm.service.file.FileVO;
import com.cmm.service.file.impl.FileMngUtil;

/** XLSX 시트 XML을 SAX로 읽는다. 전체 수집과 행 콜백이 같은 셀 해석 로직을 사용한다. */
@Component
public class ExcelSaxFileParser extends ExcelFileParser {

    public ExcelSaxFileParser(FileMngUtil fileMngUtil) {
        this(fileMngUtil, new ExcelTemplate());
    }

    @Autowired
    public ExcelSaxFileParser(FileMngUtil fileMngUtil, ExcelTemplate template) {
        super(fileMngUtil, template);
    }

    /** 행 콜백은 원본 행 전체를 보관하지 않는다. */
    public interface RowConsumer {
        default void startSheet(String sheetName) { }
        void accept(String sheetName, int rowNumber, List<String> cells);
    }

    public FileVO parseRows(MultipartFile upload, RowConsumer consumer) {
        if (consumer == null) throw new IllegalArgumentException("행 처리기가 필요합니다.");
        FileVO metadata = validate(upload);
        try (InputStream input = new BufferedInputStream(upload.getInputStream())) {
            readWorkbook(input, null, true, consumer);
            return metadata;
        } catch (IOException | RuntimeException exception) {
            throw new FileMngUtil.FileException(400, "EXCEL_PARSE_FAILED", "files", exception);
        }
    }

    @Override
    protected String extensionError(String extension) {
        return "xlsx".equals(extension) ? null : "EXCEL_SAX_EXTENSION_NOT_ALLOWED";
    }

    @Override
    protected ReadResult read(InputStream input, Definition template, boolean preview) throws IOException {
        return readWorkbook(input, template, preview, null);
    }

    /** 파일 열기 → 통합문서 설정 → 시트별 SAX 처리. */
    private ReadResult readWorkbook(InputStream input, Definition template, boolean preview, RowConsumer consumer) throws IOException {
        
    	ReadResult result = new ReadResult();
        
        try (OPCPackage pkg = OPCPackage.open(input)) {
            XSSFReader reader = new XSSFReader(pkg);
            SharedStrings strings = new ReadOnlySharedStringsTable(pkg, false);
            StylesTable styles = reader.getStylesTable();
            boolean date1904 = readDateSystem(reader);
            XSSFReader.SheetIterator sheets = (XSSFReader.SheetIterator) reader.getSheetsData();
            while (sheets.hasNext()) {
                try (InputStream inputSheet = sheets.next()) {
                    String name = sheets.getSheetName();
                    SheetMapping definition = ReadResult.find(template, name);
                    if (!preview && definition == null) continue;
                    SheetData sheet = new SheetData(name, preview, definition);
                    if (consumer != null) consumer.startSheet(name);
                    parseXml(inputSheet, new SheetHandler(sheet, strings, styles, date1904, name, consumer));
                    if (consumer == null) result.sheets.add(sheet);
                }
            }
            return result;
        } catch (OpenXML4JException | SAXException | IllegalArgumentException exception) {
            throw new IOException("XLSX 파일을 SAX 방식으로 읽을 수 없습니다.", exception);
        }
    }

    private static boolean readDateSystem(XSSFReader reader) throws IOException, OpenXML4JException {
        WorkbookHandler handler = new WorkbookHandler();
        try (InputStream workbook = reader.getWorkbookData()) {
            parseXml(workbook, handler);
        }
        return handler.date1904;
    }

    private static void parseXml(InputStream input, DefaultHandler handler) throws IOException {
        try {
            XMLReader parser = XMLHelper.newXMLReader();
            parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            parser.setFeature("http://xml.org/sax/features/external-general-entities", false);
            parser.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            parser.setContentHandler(handler);
            parser.setErrorHandler(new DefaultHandler() {
                @Override
                public void error(SAXParseException error) throws SAXException { throw error; }
                @Override
                public void fatalError(SAXParseException error) throws SAXException { throw error; }
            });
            parser.parse(new InputSource(input));
        } catch (ParserConfigurationException | SAXException exception) {
            throw new IOException("엑셀 내부 XML을 읽을 수 없습니다.", exception);
        }
    }

    private static String tag(String local, String qName) {
        return local == null || local.isEmpty() ? qName.substring(qName.indexOf(':') + 1) : local;
    }

    private static final class WorkbookHandler extends DefaultHandler {
        private boolean date1904;

        @Override
        public void startElement(String uri, String local, String qName, Attributes attributes) {
            if ("workbookPr".equals(tag(local, qName))) {
                String value = attributes.getValue("date1904");
                date1904 = "1".equals(value) || "true".equalsIgnoreCase(value);
            }
        }
    }

    private static final class SheetHandler extends DefaultHandler {
        private final SheetData sheet;
        private final SharedStrings strings;
        private final StylesTable styles;
        private final boolean date1904;
        private final String sheetName;
        private final RowConsumer consumer;
        private final DataFormatter formatter = new DataFormatter(Locale.KOREAN);
        private final StringBuilder raw = new StringBuilder();
        private final StringBuilder inline = new StringBuilder();

        private int row = -1;
        private int column = -1;
        private int style;
        private int phoneticDepth;
        private String type;
        private boolean inData, inMerges, inCell, inValue, inText;
        private boolean wanted, formula, hasValue;

        @Override
        public void startElement(String uri, String local, String qName, Attributes attributes) throws SAXException {
            String name = tag(local, qName);
            try {
                if ("sheetData".equals(name)) inData = true;
                else if ("mergeCells".equals(name)) inMerges = true;
                else if (inMerges && "mergeCell".equals(name)) addMerge(attributes);
                else if (inData && "row".equals(name)) startRow(attributes);
                else if (inData && "c".equals(name)) startCell(attributes);
                else if (inCell) startCellContent(name);
            } catch (RuntimeException exception) {
                throw new SAXException("잘못된 엑셀 셀 또는 병합 영역", exception);
            }
        }

        @Override
        public void characters(char[] text, int start, int length) {
            if (wanted && inValue) raw.append(text, start, length);
            if (wanted && inText) inline.append(text, start, length);
        }

        @Override
        public void endElement(String uri, String local, String qName) throws SAXException {
            String name = tag(local, qName);
            if ("v".equals(name)) inValue = false;
            else if ("t".equals(name)) inText = false;
            else if ("rPh".equals(name) && phoneticDepth > 0) phoneticDepth--;
            else if ("c".equals(name) && inCell) finishCell();
            else if ("row".equals(name) && inData && consumer != null) emitRow();
            else if ("sheetData".equals(name)) inData = false;
            else if ("mergeCells".equals(name)) inMerges = false;
        }

        private SheetHandler(SheetData sheet, SharedStrings strings, StylesTable styles, boolean date1904,
                String sheetName, RowConsumer consumer) {
            this.sheet = sheet;
            this.strings = strings;
            this.styles = styles;
            this.date1904 = date1904;
            this.sheetName = sheetName;
            this.consumer = consumer;
        }

        private void startRow(Attributes attributes) {
            String number = attributes.getValue("r");
            row = number == null ? row + 1 : Integer.parseInt(number) - 1;
            if (row < 0 || row >= SpreadsheetVersion.EXCEL2007.getMaxRows()) {
                throw new IllegalArgumentException("잘못된 행 번호");
            }
            sheet.lastRow = Math.max(sheet.lastRow, row);
            column = -1;
        }

        private void startCell(Attributes attributes) {
            String address = attributes.getValue("r");
            if (address == null) column++;
            else {
                CellReference reference = new CellReference(address);
                if (reference.getRow() != row) throw new IllegalArgumentException("셀과 행 번호가 다릅니다.");
                column = reference.getCol();
            }
            if (column < 0 || column >= SpreadsheetVersion.EXCEL2007.getMaxColumns()) {
                throw new IllegalArgumentException("잘못된 열 번호");
            }
            inCell = true;
            wanted = sheet.keep(row, column);
            formula = false;
            hasValue = false;
            type = attributes.getValue("t");
            String cellStyle = attributes.getValue("s");
            style = cellStyle == null ? 0 : Integer.parseInt(cellStyle);
            raw.setLength(0);
            inline.setLength(0);
            phoneticDepth = 0;
        }

        private void startCellContent(String name) {
            if ("f".equals(name)) formula = true;
            else if ("v".equals(name)) {
                inValue = true;
                hasValue = true;
            } else if ("rPh".equals(name)) phoneticDepth++;
            else if ("t".equals(name) && "inlineStr".equals(type) && phoneticDepth == 0) inText = true;
        }

        private void finishCell() throws SAXException {
            try {
                if (wanted) sheet.put(row, column, readCellValue());
            } catch (RuntimeException exception) {
                throw new SAXException("셀 값을 읽을 수 없습니다: " + new CellReference(row, column).formatAsString(), exception);
            }
            inCell = false;
            wanted = false;
            inText = false;
            inValue = false;
        }

        private void addMerge(Attributes attributes) {
            CellRangeAddress range = CellRangeAddress.valueOf(attributes.getValue("ref"));
            range.validate(SpreadsheetVersion.EXCEL2007);
            if (consumer == null) sheet.merged.add(range);
        }

        private void emitRow() {
            NavigableMap<Integer, CellData> cells = sheet.rows.get(row);
            try {
                if (cells == null || cells.isEmpty()) return;
                List<String> values = new ArrayList<>(Collections.nCopies(cells.lastKey() + 1, ""));
                boolean hasValue = false;
                for (Map.Entry<Integer, CellData> cell : cells.entrySet()) {
                    String text = cell.getValue().getFormatted();
                    values.set(cell.getKey(), text);
                    hasValue |= !text.trim().isEmpty();
                }
                if (hasValue) consumer.accept(sheetName, row + 1, values);
            } finally {
                sheet.rows.clear(); // 다음 행을 읽기 전에 현재 행의 원본 셀을 해제한다.
            }
        }

        private CellData readCellValue() {
            String text = raw.toString();
            if ("inlineStr".equals(type)) return textCell(new XSSFRichTextString(inline.toString()).getString());
            if ("s".equals(type)) return textCell(hasValue ? strings.getItemAt(Integer.parseInt(text)).getString() : "");
            if ("str".equals(type)) return textCell(text);
            if ("b".equals(type)) {
                boolean value = "1".equals(text);
                return new CellData(value, value ? "TRUE" : "FALSE", false);
            }
            if ("e".equals(type)) return new CellData(null, text, true);
            if (type != null && !"n".equals(type) && !"d".equals(type)) {
                throw new IllegalArgumentException("지원하지 않는 셀 타입: " + type);
            }
            if (!hasValue && !formula) return new CellData(null, "", false);
            return numberCell(text);
        }

        private CellData numberCell(String text) {
            double number = text.isEmpty() ? 0 : "d".equals(type) ? DateUtil.parseDateTime(text) : Double.parseDouble(text);
            XSSFCellStyle cellStyle = styles == null ? null : styles.getStyleAt(style);
            int formatIndex = cellStyle == null ? 0 : cellStyle.getDataFormat();
            String format = cellStyle == null ? "General" : cellStyle.getDataFormatString();
            boolean date = DateUtil.isValidExcelDate(number) && DateUtil.isADateFormat(formatIndex, format);
            Object value = date ? DateUtil.getJavaDate(number, date1904) : number;
            String formatted = formatter.formatRawCellContents(number, formatIndex, format, date1904);
            return new CellData(value, formatted, false);
        }

        private static CellData textCell(String text) {
            return new CellData(text, text, false);
        }
    }
}
