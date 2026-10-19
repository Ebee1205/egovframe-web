package com.cmm.service.excel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import org.apache.poi.ss.usermodel.*;
import org.springframework.mock.web.MockHttpServletResponse;
import com.cmm.service.excel.impl.DefaultExcelStyle;
import com.cmm.service.excel.impl.ExcelDomFileWriter;
import com.cmm.service.file.FileVO;
import com.event.service.EventVO;

/** 외부 DB/서버 없이 실행 가능한 회귀 검증: java ... com.cmm.service.excel.ExcelDomWriterCheck */
public class ExcelDomWriterCheck {
    private static final ExcelFileWriter WRITER = new ExcelFileWriter();
    private static final String XML = "classpath:excel/event_detail_excel.xml";
    private static int checks;

    public static void main(String[] args) throws Exception {
        try (Workbook book = open(WRITER.writeXmlTemplate(XML, null))) {
            Sheet s = book.getSheetAt(0);
            check("이벤트 상세".equals(s.getSheetName()), "sheet name");
            check(s.getRow(0).getCell(1).getCellType() == CellType.BLANK, "blank template");
            check("이벤트 상태".equals(s.getRow(8).getCell(0).getStringCellValue()), "empty repeat footer");
            check(s.getColumnWidth(0) == 12 * 256, "XML width");
            check(s.getRow(6).getHeightInPoints() == 120, "description height");
        }
        EventVO event = new EventVO();
        event.setTitle("=1+1");
        event.setDsc("첫 줄\n둘째 줄");
        event.setCreatedByNickname("작성자");
        event.setSDate(new Date(1700000000000L));
        FileVO first = new FileVO(); first.setOrignlFileNm("사진.png");
        FileVO second = new FileVO(); second.setOrignlFileNm("안내.pdf");
        event.setAttachedFiles(Arrays.asList(first, second));
        Map<String, Object> data = new HashMap<>();
        data.put("event", event); data.put("statusName", "진행중");
        ExcelStyle custom = new DefaultExcelStyle() {
            @Override public CellStyle createBodyStyle(Workbook book) {
                CellStyle style = super.createBodyStyle(book);
                style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                return style;
            }
        };
        byte[] content = WRITER.writeXml(data, XML, custom);
        try (Workbook book = open(content)) {
            Sheet s = book.getSheetAt(0);
            Cell title = s.getRow(0).getCell(1);
            check(title.getCellType() == CellType.STRING && "=1+1".equals(title.getStringCellValue()), "literal text");
            check(title.getCellStyle().getFillForegroundColor() == IndexedColors.YELLOW.getIndex(), "custom body style");
            check(s.getRow(0).getCell(4).getCellStyle().getBorderRight() == BorderStyle.THIN, "merged border");
            check(DateUtil.isCellDateFormatted(s.getRow(4).getCell(2)), "JavaBean SDate mapping");
            check("사진.png".equals(s.getRow(8).getCell(0).getStringCellValue()), "first attachment");
            check("안내.pdf".equals(s.getRow(9).getCell(0).getStringCellValue()), "second attachment");
            check("진행중".equals(s.getRow(10).getCell(1).getStringCellValue()), "shifted footer");
        }
        MockHttpServletResponse response = new MockHttpServletResponse();
        WRITER.downloadXml(response, "이벤트_상세.xlsx", data, XML, custom);
        check(response.getContentAsByteArray().length > 0, "download body");
        check(response.getHeader("Content-Disposition").contains("UTF-8"), "Korean filename");
        check(response.getHeader("Content-Type").contains("spreadsheetml"), "download MIME");
        check(Long.parseLong(response.getHeader("Content-Length")) == response.getContentAsByteArray().length, "download length");
        check(response.isCommitted(), "download flush");
        check("private, no-store".equals(response.getHeader("Cache-Control")), "cache header");
        ExcelTemplate<String> legacy = new ExcelTemplate<String>() {
            public String getSheetName() { return "기존"; }
            public List<String> getHeaders() { return Collections.singletonList("제목"); }
            public String parseRow(List<String> row) { return row.get(0); }
            public List<?> writeRow(String value) { return Collections.singletonList(value); }
        };
        try (Workbook book = open(WRITER.write(Collections.singletonList("기존 데이터"), legacy))) {
            check("기존 데이터".equals(book.getSheetAt(0).getRow(1).getCell(0).getStringCellValue()), "legacy template");
        }
        try (Workbook book = open(new ExcelDomFileWriter().writeTemplate(legacy, null))) {
            check(book.getSheetAt(0).getLastRowNum() == 0, "legacy class alias");
        }
        // 이 디렉터리는 검증 실행 시 classpath에 추가한다.
        Path fixtures = Paths.get("target/test-classes/dom-check-fixtures");
        Files.createDirectories(fixtures);
        String prefix = "<excel version=\"1\"><sheet name=\"검증\">";
        String suffix = "</sheet></excel>";
        String typed = prefix + "<column index=\"1\"/><cell range=\"A1\" property=\"number\"/>"
                + "<cell range=\"A2\" property=\"flag\"/><cell range=\"A3\" property=\"date\"/>"
                + "<cell range=\"A4\" property=\"time\"/>" + suffix;
        writeFixture(fixtures, "typed.xml", typed);
        Map<String, Object> values = new HashMap<>();
        values.put("number", 42); values.put("flag", true);
        values.put("date", LocalDate.of(2026, 10, 19));
        values.put("time", LocalDateTime.of(2026, 10, 19, 12, 30));
        try (Workbook book = open(WRITER.writeXml(values, "dom-check-fixtures/typed.xml", custom))) {
            Sheet s = book.getSheetAt(0);
            check(s.getRow(0).getCell(0).getNumericCellValue() == 42, "numeric map");
            check(s.getRow(1).getCell(0).getBooleanCellValue(), "boolean map");
            check(DateUtil.isCellDateFormatted(s.getRow(2).getCell(0)), "LocalDate map");
            check(DateUtil.isCellDateFormatted(s.getRow(3).getCell(0)), "LocalDateTime map");
            check(s.getColumnWidth(0) == custom.getColumnWidth(0), "style default width");
        }
        expectFailure(fixtures, "empty.xml", "", null);
        expectFailure(fixtures, "root.xml", "<workbook/>", null);
        expectFailure(fixtures, "xxe.xml", "<!DOCTYPE excel [<!ENTITY x SYSTEM 'file:///nonexistent'>]><excel version=\"1\">&x;</excel>", null);
        expectFailure(fixtures, "duplicate.xml", prefix + "<cell range=\"A1:B1\" text=\"x\"/><cell range=\"B1\" text=\"x\"/>" + suffix, null);
        expectFailure(fixtures, "badref.xml", prefix + "<cell range=\"XFE1\" text=\"x\"/>" + suffix, null);
        expectFailure(fixtures, "property.xml", prefix + "<cell range=\"A1\" property=\"missing\"/>" + suffix, values);
        expectFailure(fixtures, "repeat.xml", prefix + "<repeat items=\"number\"><cell range=\"A1\" text=\"x\"/></repeat>" + suffix, values);
        MockHttpServletResponse untouched = new MockHttpServletResponse();
        try {
            WRITER.downloadXml(untouched, "x.xlsx", null, "does-not-exist.xml", null);
            throw new AssertionError("missing resource accepted");
        } catch (IOException expected) { check(!untouched.isCommitted(), "no partial download on parse failure"); }
        try (org.springframework.context.annotation.AnnotationConfigApplicationContext context =
                new org.springframework.context.annotation.AnnotationConfigApplicationContext(
                    ExcelFileWriter.class, ExcelDomFileWriter.class, com.cmm.service.excel.impl.ExcelDomFileWriter.class)) {
            check(context.getBean(ExcelFileWriter.class).getClass() == ExcelFileWriter.class, "Spring primary writer");
        }
        com.event.web.EventFileController controller = new com.event.web.EventFileController(null, null, null, null, null, null);
        org.springframework.test.util.ReflectionTestUtils.setField(controller, "excelFileWriter", WRITER);
        MockHttpServletResponse templateResponse = new MockHttpServletResponse();
        controller.eventFileTemaplteDom(templateResponse);
        check(templateResponse.getContentAsByteArray().length > 0, "template controller download");
        com.event.service.EventService service = (com.event.service.EventService) java.lang.reflect.Proxy.newProxyInstance(
                ExcelDomWriterCheck.class.getClassLoader(), new Class<?>[] {com.event.service.EventService.class},
                (proxy, method, arguments) -> event);
        com.code.service.CodeFilterVO status = new com.code.service.CodeFilterVO();
        status.setCode("ACTIVE"); status.setName("진행중"); event.setStatus("ACTIVE");
        com.code.service.CodeService codes = (com.code.service.CodeService) java.lang.reflect.Proxy.newProxyInstance(
                ExcelDomWriterCheck.class.getClassLoader(), new Class<?>[] {com.code.service.CodeService.class},
                (proxy, method, arguments) -> Collections.singletonList(status));
        controller = new com.event.web.EventFileController(service, null, codes, null, null, null);
        org.springframework.test.util.ReflectionTestUtils.setField(controller, "excelFileWriter", WRITER);
        MockHttpServletResponse detailResponse = new MockHttpServletResponse();
        controller.eventFileDetailDom(1L, detailResponse);
        try (Workbook book = open(detailResponse.getContentAsByteArray())) {
            check("진행중".equals(book.getSheetAt(0).getRow(10).getCell(1).getStringCellValue()), "controller status code label");
        }
        try {
            controller.eventFileDetailDom(0L, new MockHttpServletResponse());
            throw new AssertionError("invalid ID accepted");
        } catch (org.springframework.web.server.ResponseStatusException expected) {
            check(expected.getRawStatusCode() == 400, "invalid event ID");
        }
        System.out.println("Excel DOM checks passed: " + checks);
    }

    private static Workbook open(byte[] bytes) throws IOException {
        return WorkbookFactory.create(new ByteArrayInputStream(bytes));
    }
    private static void check(boolean condition, String name) {
        if (!condition) throw new AssertionError(name);
        checks++;
    }
    private static void writeFixture(Path dir, String name, String xml) throws IOException {
        Files.write(dir.resolve(name), xml.getBytes(StandardCharsets.UTF_8));
    }
    private static void expectFailure(Path dir, String name, String xml, Object data) throws Exception {
        writeFixture(dir, name, xml);
        try {
            WRITER.writeXml(data, "dom-check-fixtures/" + name);
            throw new AssertionError("invalid XML accepted: " + name);
        } catch (IOException expected) { checks++; }
    }
}
