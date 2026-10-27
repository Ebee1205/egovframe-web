package com.cmm.service.excel.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Locale;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.model.SharedStrings;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import com.cmm.service.excel.ExcelTemplate;
import com.cmm.service.excel.ExcelTemplate.*;
import com.cmm.service.file.impl.FileMngUtil;
import com.cmm.service.excel.ExcelFileParser;
import com.cmm.service.excel.ExcelFileParser.ReadResult.*;

/** XLSX의 시트 XML을 SAX로 읽는다. 전체 XSSFWorkbook 객체를 생성하지 않는다. */
@Component
public class ExcelSaxFileParser extends ExcelFileParser {
    public ExcelSaxFileParser(FileMngUtil fileMngUtil) { this(fileMngUtil, new ExcelTemplate()); }
    @Autowired
    public ExcelSaxFileParser(FileMngUtil fileMngUtil, ExcelTemplate template) { super(fileMngUtil, template); }

    @Override
    protected String extensionError(String extension) {
        return "xlsx".equals(extension) ? null : "EXCEL_SAX_EXTENSION_NOT_ALLOWED";
    }
    @Override
    protected ReadResult read(InputStream input, Definition template, boolean preview) throws IOException {
        ReadResult result = new ReadResult();
        try (OPCPackage pkg = OPCPackage.open(input)) {
            XSSFReader reader = new XSSFReader(pkg);
            SharedStrings strings = new ReadOnlySharedStringsTable(pkg, false);
            StylesTable styles = reader.getStylesTable();
            final boolean[] date1904 = {false};
            try (InputStream workbook = reader.getWorkbookData()) {
                parseXml(workbook, new DefaultHandler() {
                    @Override public void startElement(String uri, String local, String qName, Attributes attributes) {
                        if ("workbookPr".equals(tag(local, qName))) {
                            String value = attributes.getValue("date1904");
                            date1904[0] = "1".equals(value) || "true".equalsIgnoreCase(value);
                        }
                    }
                });
            }
            Iterator<InputStream> iterator = reader.getSheetsData();
            XSSFReader.SheetIterator sheets = (XSSFReader.SheetIterator) iterator;
            while (sheets.hasNext()) {
                try (InputStream sheet = sheets.next()) {
                    String name = sheets.getSheetName();
                    SheetMapping definition = ReadResult.find(template, name);
                    if (!preview && definition == null) continue;
                    SheetData target = new SheetData(name, preview, definition);
                    parseXml(sheet, new SheetHandler(target, strings, styles, date1904[0]));
                    result.sheets.add(target);
                }
            }
            return result;
        } catch (OpenXML4JException | SAXException | IllegalArgumentException exception) {
            throw new IOException("XLSX 파일을 SAX 방식으로 읽을 수 없습니다.", exception);
        }
    }

    private static String tag(String local, String qName) {
        return local == null || local.isEmpty() ? qName.substring(qName.indexOf(':') + 1) : local;
    }
    private static void parseXml(InputStream input, DefaultHandler handler) throws IOException {
        try {
            XMLReader parser = XMLHelper.newXMLReader();
            parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            parser.setFeature("http://xml.org/sax/features/external-general-entities", false);
            parser.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            parser.setContentHandler(handler);
            parser.setErrorHandler(new DefaultHandler() {
                @Override public void error(SAXParseException error) throws SAXException { throw error; }
                @Override public void fatalError(SAXParseException error) throws SAXException { throw error; }
            });
            parser.parse(new InputSource(input));
        } catch (ParserConfigurationException | SAXException exception) {
            throw new IOException("엑셀 내부 XML을 읽을 수 없습니다.", exception);
        }
    }

    private static final class SheetHandler extends DefaultHandler {
        private final SheetData sheet;
        private final SharedStrings strings;
        private final StylesTable styles;
        private final boolean date1904;
        private final DataFormatter formatter = new DataFormatter(Locale.KOREAN);
        private final StringBuilder raw = new StringBuilder(), inline = new StringBuilder();
        private int row = -1, column = -1, style, phoneticDepth;
        private boolean inData, inMerges, inCell, inValue, inText, wanted, formula, hasValue;
        private String type;

        SheetHandler(SheetData sheet, SharedStrings strings, StylesTable styles, boolean date1904) {
            this.sheet = sheet; this.strings = strings; this.styles = styles; this.date1904 = date1904;
        }
        @Override
        public void startElement(String uri, String local, String qName, Attributes attributes) throws SAXException {
            String name = tag(local, qName);
            try {
                if ("sheetData".equals(name)) inData = true;
                else if ("mergeCells".equals(name)) inMerges = true;
                else if (inMerges && "mergeCell".equals(name)) {
                    CellRangeAddress range = CellRangeAddress.valueOf(attributes.getValue("ref"));
                    range.validate(SpreadsheetVersion.EXCEL2007); sheet.merged.add(range);
                } else if (inData && "row".equals(name)) {
                    row = attributes.getValue("r") == null ? row + 1 : Integer.parseInt(attributes.getValue("r")) - 1;
                    if (row < 0 || row >= SpreadsheetVersion.EXCEL2007.getMaxRows()) throw new IllegalArgumentException("잘못된 행 번호");
                    sheet.lastRow = Math.max(sheet.lastRow, row); column = -1;
                } else if (inData && "c".equals(name)) {
                    String address = attributes.getValue("r");
                    if (address == null) column++;
                    else {
                        CellReference reference = new CellReference(address);
                        if (reference.getRow() != row) throw new IllegalArgumentException("셀과 행 번호가 다릅니다.");
                        column = reference.getCol();
                    }
                    if (column < 0 || column >= SpreadsheetVersion.EXCEL2007.getMaxColumns()) throw new IllegalArgumentException("잘못된 열 번호");
                    inCell = true; wanted = sheet.keep(row, column); formula = false; hasValue = false;
                    type = attributes.getValue("t");
                    style = attributes.getValue("s") == null ? 0 : Integer.parseInt(attributes.getValue("s"));
                    raw.setLength(0); inline.setLength(0); phoneticDepth = 0;
                } else if (inCell) {
                    if ("f".equals(name)) formula = true;
                    else if ("v".equals(name)) { inValue = true; hasValue = true; }
                    else if ("rPh".equals(name)) phoneticDepth++;
                    else if ("t".equals(name) && "inlineStr".equals(type) && phoneticDepth == 0) inText = true;
                }
            } catch (RuntimeException exception) { throw new SAXException("잘못된 엑셀 셀 또는 병합 영역", exception); }
        }
        @Override public void characters(char[] text, int start, int length) {
            if (wanted && inValue) raw.append(text, start, length);
            if (wanted && inText) inline.append(text, start, length);
        }
        @Override
        public void endElement(String uri, String local, String qName) throws SAXException {
            String name = tag(local, qName);
            if ("v".equals(name)) inValue = false;
            else if ("t".equals(name)) inText = false;
            else if ("rPh".equals(name) && phoneticDepth > 0) phoneticDepth--;
            else if ("c".equals(name) && inCell) {
                try { if (wanted) sheet.put(row, column, value()); }
                catch (RuntimeException exception) { throw new SAXException("셀 값을 읽을 수 없습니다: " + new CellReference(row, column).formatAsString(), exception); }
                inCell = false; wanted = false; inText = false; inValue = false;
            } else if ("sheetData".equals(name)) inData = false;
            else if ("mergeCells".equals(name)) inMerges = false;
        }
        private CellData value() {
            String text = raw.toString();
            if ("inlineStr".equals(type)) {
                String value = new XSSFRichTextString(inline.toString()).getString();
                return new CellData(value, value, false);
            }
            if ("s".equals(type)) {
                String value = !hasValue ? "" : strings.getItemAt(Integer.parseInt(text)).getString();
                return new CellData(value, value, false);
            }
            if ("str".equals(type)) return new CellData(text, text, false);
            if ("b".equals(type)) {
                boolean value = "1".equals(text); return new CellData(value, value ? "TRUE" : "FALSE", false);
            }
            if ("e".equals(type)) return new CellData(null, text, true);
            if (type != null && !"n".equals(type) && !"d".equals(type)) throw new IllegalArgumentException("지원하지 않는 셀 타입: " + type);
            if (!hasValue && !formula) return new CellData(null, "", false);
            double number = text.isEmpty() ? 0 : "d".equals(type) ? DateUtil.parseDateTime(text) : Double.parseDouble(text);
            XSSFCellStyle cellStyle = styles == null ? null : styles.getStyleAt(style);
            int formatIndex = cellStyle == null ? 0 : cellStyle.getDataFormat();
            String format = cellStyle == null ? "General" : cellStyle.getDataFormatString();
            boolean date = DateUtil.isValidExcelDate(number) && DateUtil.isADateFormat(formatIndex, format);
            Object value = date ? DateUtil.getJavaDate(number, date1904) : number;
            String formatted = formatter.formatRawCellContents(number, formatIndex, format, date1904);
            return new CellData(value, formatted, false);
        }
    }
}
