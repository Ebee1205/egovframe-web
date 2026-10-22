package com.event;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.zip.*;
import javax.json.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.mock.web.*;
import org.springframework.test.util.ReflectionTestUtils;
import com.code.service.*;
import com.reg.service.*;
import com.event.service.*;
import com.event.web.EventFileController;
import com.cmm.service.excel.*;
import com.cmm.service.excel.impl.*;
import com.cmm.service.file.impl.FileMngUtil;

/** DB/스토리지 없이 다운로드의 실제 데이터 검증 목록과 업로드 역매핑을 확인한다. */
public class ExcelDropdownCheck {
    static int checks;
    static final String XML="classpath:excel/EventDetail_Excel.xml";
    static final ExcelTemplate TEMPLATES=new ExcelTemplate();
    public static void main(String[] args) throws Exception {
        List<String> queried=new ArrayList<>();
        CodeService codes=proxy(CodeService.class,(method,values)-> {
            if(!method.equals("selectCmmCodeDetail"))throw new AssertionError("unexpected code call");
            String key=((CodeFilterVO)values[0]).getParentCode(); queried.add(key);
            List<CodeFilterVO> list=new ArrayList<>();
            for(int i=0;i<(key.equals("EVENT_CTG_ROOT")?300:2);i++) {
                CodeFilterVO c=new CodeFilterVO(); c.setCode(key.equals("EVENT_CTG_ROOT")?String.format("CAT%03d",i):key.equals("TAG_ROOT")?"TAG"+i:i==0?"OPEN":"CLOSED");
                c.setName("표시명"+i);list.add(c);
            }
            return list;
        });
        List<RegFilterVO> regions=Arrays.asList(region(1,null,"SEOUL"),region(2,1L,"SEOUL_JUNG"),region(3,2L,"SEOUL_JUNG_DONG"));
        RegService regs=proxy(RegService.class,(method,values)-> {
            if(method.equals("selectRegRoot"))return Collections.singletonList(regions.get(0));
            if(method.equals("selectRegParents"))return regions;
            throw new AssertionError("unexpected region call");
        });
        EventFileController optionsController=new EventFileController(null,null,codes,null,regs,null);
        ExcelTemplate.Definition definition=TEMPLATES.load(XML);
        Map<String,List<String>> lists=ReflectionTestUtils.invokeMethod(optionsController,"loadExcelOptions",definition);
        check(lists.size()==3,"only XML fields queried");
        check(!queried.contains("TAG_ROOT"),"no unmapped tags");
        check(lists.get("REGION").equals(Arrays.asList("서울특별시","서울특별시 중구")),"Korean region choices");
        check(lists.get("EVENT_CTG_ROOT").size()==300,"long code list");
        ExcelFileWriter writer=new ExcelFileWriter();
        byte[] blank=writer.writeXml(null,XML,new com.event.service.impl.EventExcelStyle(),lists);
        verify(blank,true);
        // 기존 API는 코드 목록 인수 없이도 계속 사용할 수 있다.
        try(Workbook book=WorkbookFactory.create(new ByteArrayInputStream(writer.writeXml(null,XML)))) {
            check(book.getSheetAt(0).getDataValidations().isEmpty(),"legacy writer API remains usable");
        }
        Map<String,List<String>> missing=new HashMap<>(lists); missing.remove("REGION");
        try {writer.writeXml(null,XML,null,missing);throw new AssertionError("missing list accepted");}
        catch(IOException expected){checks++;}
        missing.put("REGION",Collections.emptyList());
        try {writer.writeXml(null,XML,null,missing);throw new AssertionError("empty list accepted");}
        catch(IOException expected){checks++;}

        EventVO event=new EventVO();event.setEid(5L);event.setReg_id(2L);event.setRegName("중구");event.setTitle("검증 이벤트");
        event.setCtg("CAT000");event.setStatus("OPEN");event.setDsc("설명");event.setAddress("서울");
        java.text.SimpleDateFormat format=new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
        event.setSDate(format.parse("2026-10-22 09:00"));event.setEDate(format.parse("2026-10-22 18:00"));
        EventService events=proxy(EventService.class,(method,values)-> {
            if(method.equals("selectEvent"))return event;
            throw new AssertionError("persistence accessed: "+method);
        });
        org.springframework.context.support.StaticMessageSource messages=new org.springframework.context.support.StaticMessageSource();
        messages.addMessage("success.request.msg",Locale.KOREAN,"완료");
        EventFileController controller=new EventFileController(events,null,codes,null,regs,messages);
        ReflectionTestUtils.setField(controller,"excelFileWriter",writer);
        ReflectionTestUtils.setField(controller,"excelFileParser",new ExcelDomFileParser(new FileMngUtil("xlsx.xls","",52428800,20,"")));
        MockHttpServletResponse templateResponse=new MockHttpServletResponse();controller.eventFileTemapltedownload(templateResponse);
        verify(templateResponse.getContentAsByteArray(),true);
        check(templateResponse.getContentType().contains("spreadsheetml"),"template MIME");
        MockHttpServletResponse detailResponse=new MockHttpServletResponse();controller.eventFileDetailDom(5L,detailResponse);
        verify(detailResponse.getContentAsByteArray(),false);
        org.springframework.http.ResponseEntity<String> upload=controller.eventFileTemplateUpload(Collections.singletonList(new MockMultipartFile("files","detail.xlsx","application/octet-stream",detailResponse.getContentAsByteArray())),"");
        check(upload.getStatusCodeValue()==200,"downloaded codes upload successfully");
        try(JsonReader reader=Json.createReader(new StringReader(upload.getBody()))) {
            JsonObject parsed=reader.readObject().getJsonArray("data").getJsonObject(0).getJsonObject("fileContent").getJsonObject("event");
            check(parsed.getString("ctg").equals("CAT000"),"category code roundtrip");
            check(parsed.getString("status").equals("OPEN"),"status code roundtrip");
            check(parsed.getInt("reg_id")==2,"English region code resolves ID");
            check(!parsed.containsKey("eid"),"server-managed ID excluded");
        }
        byte[] invalid;
        try(Workbook book=WorkbookFactory.create(new ByteArrayInputStream(detailResponse.getContentAsByteArray()));ByteArrayOutputStream out=new ByteArrayOutputStream()) {
            book.getSheetAt(0).getRow(1).getCell(1).setCellValue("UNKNOWN");book.write(out);invalid=out.toByteArray();
        }
        check(controller.eventFileTemplateUpload(Collections.singletonList(new MockMultipartFile("files","invalid.xlsx","application/octet-stream",invalid)),"").getStatusCodeValue()==400,"pasted invalid codes rejected on server");

        Path fixtures=Paths.get(args[0],"fixtures");Files.createDirectories(fixtures);
        Files.write(fixtures.resolve("tag.xml"),"<excel version=\"1\"><sheet name=\"Tags\"><cell range=\"A1\" property=\"tag\" dropdown=\"TAG_ROOT\"/></sheet></excel>".getBytes(StandardCharsets.UTF_8));
        ExcelTemplate.Definition tag=TEMPLATES.load("fixtures/tag.xml");
        Map<String,List<String>> tags=ReflectionTestUtils.invokeMethod(optionsController,"loadExcelOptions",tag);
        check(tags.get("TAG_ROOT").equals(Arrays.asList("표시명0","표시명1")),"new XML group uses CodeService automatically");
        try(Workbook book=WorkbookFactory.create(new ByteArrayInputStream(new ExcelDomFileWriter().write(null,tag,null,tags)))) {
            check(book.getSheetAt(0).getDataValidations().size()==1,"TAG_ROOT dropdown metadata supported");
        }
        String xml=zip(blank,"xl/worksheets/sheet1.xml");
        check(xml.contains("showDropDown=\"false\""),"native dropdown arrow visible in serialized XLSX");
        check(lists.get("EVENT_CTG_ROOT").get(0).equals("표시명0"),"Korean category choices");
        System.out.println("Excel dropdown checks passed: "+checks);
    }
    static void verify(byte[] bytes,boolean blank)throws Exception {
        try(Workbook book=WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            Sheet sheet=book.getSheetAt(0);
            check(book.getNumberOfSheets()==2,"event and lookup sheets");
            check(book.getSheetVisibility(1)==SheetVisibility.VERY_HIDDEN,"hidden lookup sheet");
            check(sheet.getNumMergedRegions()==7,"XML merged layout preserved");
            List<? extends DataValidation> validations=sheet.getDataValidations();
            check(validations.size()==3,"three dropdowns");
            String[] expected={"B2","B3","B8"};
            for(int i=0;i<3;i++) {
                DataValidation v=validations.get(i);
                check(v.getRegions().getCellRangeAddresses()[0].formatAsString().equals(expected[i]),"XML anchor "+expected[i]);
                check(v.getErrorStyle()==DataValidation.ErrorStyle.STOP && v.getShowErrorBox(),"stop invalid list entries");
                check(book.getName(v.getValidationConstraint().getFormula1())!=null,"named-range validation");
            }
            check(book.getAllNames().get(0).getRefersToFormula().endsWith("$300"),"list above 255 characters uses full range");
            check(sheet.getRow(1).getCell(1).getCellStyle().getFillPattern()==FillPatternType.NO_FILL,"body no fill");
            check(((org.apache.poi.xssf.usermodel.XSSFCellStyle)sheet.getRow(0).getCell(0).getCellStyle()).getFillForegroundXSSFColor().getARGBHex().endsWith("D9D9D9"),"gray header");
            if(blank)check(sheet.getRow(1).getCell(1).toString().isEmpty(),"blank create template");
            else {
                check(sheet.getRow(1).getCell(1).getStringCellValue().equals("표시명0"),"detail category displayed in Korean");
                check(sheet.getRow(2).getCell(1).getStringCellValue().equals("서울특별시 중구"),"detail region displayed in Korean");
                check(sheet.getRow(7).getCell(1).getStringCellValue().equals("표시명0"),"detail status displayed in Korean");
            }
        }
    }
    static String zip(byte[] bytes,String name)throws Exception {try(ZipInputStream in=new ZipInputStream(new ByteArrayInputStream(bytes))){ZipEntry e;while((e=in.getNextEntry())!=null)if(e.getName().equals(name)){ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[4096];int n;while((n=in.read(b))!=-1)out.write(b,0,n);return new String(out.toByteArray(),StandardCharsets.UTF_8);}}throw new AssertionError(name);}
    static RegFilterVO region(long id,Long parent,String code){RegFilterVO r=new RegFilterVO();r.setReg_id(id);r.setParentRegId(parent);r.setCode(code);r.setName(id==1?"서울특별시":id==2?"중구":"동");return r;}
    interface Calls{Object invoke(String method,Object[] values)throws Exception;}
    @SuppressWarnings("unchecked") static <T>T proxy(Class<T>type,Calls calls){return(T)Proxy.newProxyInstance(type.getClassLoader(),new Class<?>[]{type},(p,m,a)->m.getDeclaringClass()==Object.class?type.getSimpleName():calls.invoke(m.getName(),a));}
    static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);checks++;}
}
