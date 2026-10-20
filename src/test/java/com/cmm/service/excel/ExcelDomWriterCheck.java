package com.cmm.service.excel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import org.apache.poi.ss.usermodel.*;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import com.cmm.service.excel.impl.DefaultExcelStyle;
import com.cmm.service.excel.impl.ExcelDomFileWriter;
import com.cmm.service.excel.impl.ExcelFileParserUtil;
import com.cmm.service.file.FileVO;
import com.cmm.service.file.impl.FileMngUtil;
import com.event.service.EventVO;

/** 외부 DB/서버 없이 XML 공통 양식의 생성/역매핑을 검증한다. */
public class ExcelDomWriterCheck {
    private static final String XML = "classpath:excel/EventDetail_Excel.xml";
    private static final ExcelTemplate TEMPLATES = new ExcelTemplate();
    private static final ExcelFileWriter WRITER = new ExcelFileWriter(TEMPLATES);
    private static final ExcelFileParserUtil PARSER = new ExcelFileParserUtil(
            new FileMngUtil("xlsx.xls", "png", 52428800, 20, ""), TEMPLATES);
    private static int checks;

    public static void main(String[] args) throws Exception {
        ExcelTemplate.Definition shared = TEMPLATES.load(XML);
        check(shared.getSheets().size() == 1, "shared sheet definition");
        try { shared.getSheets().clear(); throw new AssertionError("mutable template"); }
        catch (UnsupportedOperationException expected) { checks++; }
        byte[] blank = WRITER.writeMapped(null, shared, null);
        try (Workbook book = open(blank)) {
            Sheet sheet = book.getSheetAt(0);
            check("이벤트 상세".equals(sheet.getSheetName()), "sheet name");
            check(sheet.getColumnWidth(0) == 14 * 256, "current XML width");
            check("이벤트 카테고리".equals(sheet.getRow(1).getCell(0).getStringCellValue()), "current XML category row");
            check("이벤트 상태".equals(sheet.getRow(9).getCell(0).getStringCellValue()), "zero repeat footer");
            check(sheet.getRow(7).getHeightInPoints() == 120, "description height");
        }
        Map<String, Object> empty = PARSER.parseMapped(upload(blank), shared);
        check(event(empty).get("title") == null, "blank value");
        check(files(empty).isEmpty(), "empty repeat reverse mapping");
        EventVO event = new EventVO();
        event.setTitle("=1+1"); event.setCtg("CTG001"); event.setDsc("첫 줄\n둘째 줄");
        event.setCreatedByNickname("작성자"); event.setAddress("서울");
        event.setSDate(new Date(1700000000000L));
        FileVO first = new FileVO(); first.setOrignlFileNm("이벤트 상태");
        FileVO second = new FileVO(); second.setOrignlFileNm("안내.pdf");
        event.setAttachedFiles(Arrays.asList(first, second));
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("event", event); data.put("statusName", "진행중");
        ExcelStyle style = new DefaultExcelStyle() {
            @Override public CellStyle createBodyStyle(Workbook book) {
                CellStyle body = super.createBodyStyle(book);
                body.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
                body.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                return body;
            }
        };
        byte[] content = WRITER.writeMapped(data, shared, style);
        try (Workbook book = open(content)) {
            Sheet sheet = book.getSheetAt(0);
            Cell title = sheet.getRow(0).getCell(1);
            check(title.getCellType() == CellType.STRING, "no formula execution");
            check(title.getCellStyle().getFillForegroundColor() == IndexedColors.YELLOW.getIndex(), "custom style");
            check(sheet.getRow(0).getCell(4).getCellStyle().getBorderRight() == BorderStyle.THIN, "merged border");
            check(DateUtil.isCellDateFormatted(sheet.getRow(5).getCell(2)), "JavaBean SDate");
            check("진행중".equals(sheet.getRow(11).getCell(1).getStringCellValue()), "repeat offset footer");
        }
        Map<String, Object> parsed = PARSER.parseMapped(upload(content), shared);
        check("=1+1".equals(event(parsed).get("title")), "nested property reverse mapping");
        check("CTG001".equals(event(parsed).get("ctg")), "category reverse mapping");
        check("첫 줄\n둘째 줄".equals(event(parsed).get("dsc")), "multiline reverse mapping");
        check(Math.abs(((Date) event(parsed).get("SDate")).getTime() - event.getSDate().getTime()) <= 1, "date reverse mapping");
        check(files(parsed).size() == 2, "repeat count");
        check("이벤트 상태".equals(files(parsed).get(0).get("orignlFileNm")), "footer-like filename");
        check("안내.pdf".equals(files(parsed).get(1).get("orignlFileNm")), "second repeat item");
        check("진행중".equals(parsed.get("statusName")), "footer reverse mapping");
        // 동일한 Definition 객체를 재사용하며 출력->입력->출력->입력을 확인한다.
        Map<String, Object> roundtrip = PARSER.parseMapped(upload(WRITER.writeMapped(parsed, shared, null)), shared);
        check(parsed.equals(roundtrip), "round trip shared template");
        check(PARSER.parseXml(upload(content), XML).equals(parsed), "resource parser convenience");
        List<Map<String, Object>> preview = PARSER.parse(Collections.singletonList(upload(content)), "");
        check(!((Map<?, ?>) preview.get(0).get("fileContent")).containsKey("data"), "legacy raw preview");
        List<Map<String, Object>> mapped = PARSER.parseXml(Collections.singletonList(upload(content)), "", XML);
        Map<?, ?> fileContent = (Map<?, ?>) mapped.get(0).get("fileContent");
        check(fileContent.containsKey("sheets") && parsed.equals(fileContent.get("data")), "mapped upload keeps preview");
        check(com.cmm.service.ComResponseVO.success(mapped, "완료").toJson().contains("statusName"), "JSON upload response");
        MockHttpServletResponse response = new MockHttpServletResponse();
        WRITER.downloadXml(response, "이벤트_상세.xlsx", data, XML, style);
        check(response.getContentType().contains("spreadsheetml"), "MIME type");
        check(response.getHeader("Content-Disposition").contains("UTF-8"), "Korean filename");
        check(response.getContentLengthLong() == response.getContentAsByteArray().length, "download length");
        check(response.isCommitted(), "download flush");

        Path fixtures = Paths.get("target/test-classes/shared-template-check");
        Files.createDirectories(fixtures);
        String head = "<excel version=\"1\"><sheet name=\"검증\">", tail = "</sheet></excel>";
        String typedXml = head + "<column index=\"1\"/><cell range=\"A1\" property=\"number\"/>"
                + "<cell range=\"A2\" property=\"flag\"/><cell range=\"A3\" property=\"date\"/>" + tail;
        ExcelTemplate.Definition typed = fixture(fixtures, "typed.xml", typedXml);
        Map<String, Object> values = new HashMap<>();
        values.put("number", 42); values.put("flag", true); values.put("date", LocalDate.of(2026, 10, 20));
        byte[] typedContent = WRITER.writeMapped(values, typed, style);
        Map<String, Object> typedParsed = PARSER.parseMapped(upload(typedContent), typed);
        check(Double.valueOf(42).equals(typedParsed.get("number")), "numeric mapping");
        check(Boolean.TRUE.equals(typedParsed.get("flag")), "boolean mapping");
        check(typedParsed.get("date") instanceof Date, "date cell type");
        try (Workbook book = open(typedContent)) { check(book.getSheetAt(0).getColumnWidth(0) == style.getColumnWidth(0), "style default width"); }
        expectBadTemplate(fixtures, "empty.xml", "");
        expectBadTemplate(fixtures, "root.xml", "<workbook/>");
        expectBadTemplate(fixtures, "xxe.xml", "<!DOCTYPE excel [<!ENTITY x SYSTEM 'file:///nonexistent'>]><excel version=\"1\">&x;</excel>");
        expectBadTemplate(fixtures, "overlap.xml", head + "<cell range=\"A1:B1\" text=\"x\"/><cell range=\"B1\" text=\"y\"/>" + tail);
        expectBadTemplate(fixtures, "range.xml", head + "<cell range=\"XFE1\" text=\"x\"/>" + tail);
        expectBadTemplate(fixtures, "width.xml", head + "<column index=\"1\" width=\"NaN\"/><cell range=\"A1\" text=\"x\"/>" + tail);
        expectBadTemplate(fixtures, "repeatrows.xml", head + "<repeat items=\"x\"><cell range=\"A1\" text=\"x\"/><cell range=\"A2\" text=\"x\"/></repeat>" + tail);
        expectBadTemplate(fixtures, "path.xml", head + "<cell range=\"A1\" property=\"event.class\"/>" + tail);
        try { WRITER.writeMapped(Collections.emptyMap(), typed, null); throw new AssertionError("missing map key accepted"); }
        catch (IOException expected) { checks++; }
        try (Workbook book = open(content)) {
            book.getSheetAt(0).getRow(0).getCell(0).setCellValue("다른 제목");
            expectBadWorkbook(book, shared, "fixed label validation");
        }
        try (Workbook book = open(content)) {
            book.getSheetAt(0).removeMergedRegion(0);
            expectBadWorkbook(book, shared, "merge validation");
        }
        try (Workbook book = open(content)) {
            book.setSheetName(0, "다른 시트");
            expectBadWorkbook(book, shared, "sheet validation");
        }
        try (Workbook book = open(content)) {
            book.getSheetAt(0).getRow(11).getCell(0).setCellValue("잘못된 끝");
            expectBadWorkbook(book, shared, "repeat footer validation");
        }
        try (org.springframework.context.annotation.AnnotationConfigApplicationContext context =
                new org.springframework.context.annotation.AnnotationConfigApplicationContext(
                        ExcelTemplate.class, ExcelFileWriter.class, ExcelDomFileWriter.class)) {
            check(context.getBean(ExcelFileWriter.class) != null, "Spring common template injection");
        }
        org.springframework.context.support.StaticMessageSource messages = new org.springframework.context.support.StaticMessageSource();
        messages.addMessage("success.request.msg", Locale.KOREAN, "완료");
        com.event.web.EventFileController controller = new com.event.web.EventFileController(null, null, null, null, null, messages);
        ReflectionTestUtils.setField(controller, "excelFileWriter", WRITER);
        ReflectionTestUtils.setField(controller, "excelFileParser", PARSER);
        MockHttpServletResponse templateResponse = new MockHttpServletResponse();
        controller.eventFileTemapltedownload(templateResponse);
        check(files(PARSER.parseMapped(upload(templateResponse.getContentAsByteArray()), shared)).isEmpty(), "controller template uses same XML");
        check(controller.eventFileTemplateUpload(Collections.singletonList(upload(content)), "").getBody().contains("statusName"), "controller XML upload");
        System.out.println("Shared Excel template checks passed: " + checks);
    }

    private static MockMultipartFile upload(byte[] content) { return new MockMultipartFile("files", "이벤트.xlsx", "application/octet-stream", content); }
    private static Workbook open(byte[] content) throws IOException { return WorkbookFactory.create(new ByteArrayInputStream(content)); }
    @SuppressWarnings("unchecked") private static Map<String, Object> event(Map<String, Object> data) { return (Map<String, Object>) data.get("event"); }
    @SuppressWarnings("unchecked") private static List<Map<String, Object>> files(Map<String, Object> data) { return (List<Map<String, Object>>) event(data).get("attachedFiles"); }
    private static void check(boolean condition, String name) { if (!condition) throw new AssertionError(name); checks++; }
    private static ExcelTemplate.Definition fixture(Path dir, String name, String xml) throws IOException {
        Files.write(dir.resolve(name), xml.getBytes(StandardCharsets.UTF_8));
        return TEMPLATES.load("shared-template-check/" + name);
    }
    private static void expectBadTemplate(Path dir, String name, String xml) throws Exception {
        try { fixture(dir, name, xml); throw new AssertionError("invalid template accepted: " + name); }
        catch (IOException expected) { checks++; }
    }
    private static void expectBadWorkbook(Workbook book, ExcelTemplate.Definition template, String name) throws Exception {
        java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream(); book.write(output);
        try { PARSER.parseMapped(upload(output.toByteArray()), template); throw new AssertionError(name); }
        catch (IOException expected) { checks++; }
    }
}
