package com.cmm.service.excel.impl;
import java.io.*;
import java.text.*;
import java.util.*;
import com.event.service.*;
import com.code.service.*;
import com.reg.service.*;
import com.cmm.service.excel.*;
import com.cmm.service.excel.ExcelTemplate.*;
import com.cmm.service.file.impl.FileMngUtil;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springmodules.validation.commons.DefaultValidatorFactory;

/** 이벤트 엑셀의 조회, 매핑, 검증과 등록을 조합한다. HTTP 응답은 Controller에서 처리한다. */
@Service
public class ExcelFileServiceImpl {
    private final EventService eventService;
    private final CodeService codeService;
    private final RegService regService;
    @Autowired private ExcelDomFileParser excelFileParser;
    @Autowired private ExcelFileWriter excelFileWriter;
    @Autowired private DefaultValidatorFactory validatorFactory;
    public ExcelFileServiceImpl(EventService eventService, CodeService codeService, RegService regService) {
        this.eventService=eventService; this.codeService=codeService; this.regService=regService;
    }
    public void writeEventList(EventFilterVO filterVO, OutputStream output) throws Exception {
        filterVO.setPageIndex(1);
        filterVO.setPageSize(0);

        List<EventVO> events = eventService.selectEventList(filterVO);
        if (events.size() >= org.apache.poi.ss.SpreadsheetVersion.EXCEL2007.getMaxRows()) {
            throw new IllegalArgumentException("검색 범위를 줄여주세요. 엑셀 최대 행 수를 초과했습니다.");
        }

        Map<String, String> categories = new HashMap<>();
        Map<String, String> statuses = new HashMap<>();
        CodeFilterVO codeFilter = new CodeFilterVO();

        codeFilter.setParentCode("EVENT_CTG_ROOT");
        for (CodeFilterVO code : codeService.selectCmmCodeDetail(codeFilter)) categories.put(code.getCode(), code.getName());

        codeFilter.setParentCode("EVENT_STAT_ROOT");
        for (CodeFilterVO code : codeService.selectCmmCodeDetail(codeFilter)) statuses.put(code.getCode(), code.getName());

        final int[] number = {0};

        new com.cmm.service.excel.impl.ExcelSXSSFWriter().writeList(output, "이벤트 목록",
                new String[] {"NO", "지역", "제목", "분류", "시작일", "종료일", "주소", "등록일", "수정일", "상태"},
                new int[] {8, 20, 50, 16, 22, 22, 50, 22, 22, 16}, events,
                event -> new Object[] {
	                		++number[0],
	                		event.getRegName(),
	                		event.getTitle(),
	                		categories.getOrDefault(event.getCtg(), event.getCtg()),
	                		event.getSDate(), event.getEDate(),
	                		event.getAddress(),
	                		event.getCDate(), event.getUDate(),
	                		statuses.getOrDefault(event.getStatus(), event.getStatus())
                		},
                new com.event.service.impl.EventExcelStyle());

    }

    public byte[] writeEventDetail(Long eid) throws Exception {
        if (eid == null || eid <= 0) {
            throw new IllegalArgumentException("이벤트 ID가 올바르지 않습니다.");
        }

        EventVO query = new EventVO();
        query.setEid(eid);
        EventVO event = eventService.selectEvent(query);

        if (event == null) {
            throw new NoSuchElementException("이벤트를 찾을 수 없습니다.");
        }

        com.cmm.service.excel.ExcelTemplate.Definition template = new com.cmm.service.excel.ExcelTemplate().load("classpath:excel/EventDetail_Excel.xml");
        Map<String, List<String>> options = loadExcelOptions(template);
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> eventData = new HashMap<>();

        eventData.put("title", event.getTitle());
        eventData.put("regName", excelRegionLabel(event.getReg_id()));
        eventData.put("address", event.getAddress());
        eventData.put("SDate", event.getSDate());
        eventData.put("EDate", event.getEDate());
        eventData.put("dsc", event.getDsc());
        eventData.put("categoryName", excelCommonLabel("EVENT_CTG_ROOT", event.getCtg()));
        eventData.put("statusName", excelCommonLabel("EVENT_STAT_ROOT", event.getStatus()));
        data.put("event", eventData);

        return excelFileWriter.writeXml(data, "classpath:excel/EventDetail_Excel.xml", new com.event.service.impl.EventExcelStyle(), options);
    }

    public byte[] writeEventTemplate() throws Exception {
        String resource = "classpath:excel/EventDetail_Excel.xml";
        com.cmm.service.excel.ExcelTemplate.Definition template = new com.cmm.service.excel.ExcelTemplate().load(resource);
        return excelFileWriter.writeXml(null, resource,
                new com.event.service.impl.EventExcelStyle(), loadExcelOptions(template));
    }

    public List<Map<String, Object>> parseEventExcel(List<MultipartFile> files, String description) throws Exception {
        return parseEventExcel(files, description, null);
    }

    private List<Map<String, Object>> parseEventExcel(List<MultipartFile> files, String description, java.util.function.Consumer<EventVO> parsed) throws Exception {
        if (files == null || files.size() != 1) throw FileMngUtil.bad("FILE_COUNT_INVALID", "files");
            List<Map<String, Object>> data = excelFileParser.parseXml(files, description, "classpath:excel/EventDetail_Excel.xml");
           
            @SuppressWarnings("unchecked")
            Map<String, Object> content = (Map<String, Object>) data.get(0).get("fileContent");
            
            @SuppressWarnings("unchecked")
            Map<String, Object> mapped = (Map<String, Object>) content.get("data");
            Map<String, Object> preview = new LinkedHashMap<>();
            
            EventVO eventVO = parseExcelEvent(mapped, preview);
            content.put("event", preview);
            
            if (parsed != null) parsed.accept(eventVO);
        return data;
    }
    
    public Map<String, Long> insertEventExcel(List<MultipartFile> files, String description, String createdBy) throws Exception {
        final EventVO[] parsed = new EventVO[1];
        
        parseEventExcel(files, description, event -> parsed[0] = event);
        EventVO eventVO = parsed[0];
        
        // 등록자 정보는 엑셀에서 읽지 않고 기존 신규 등록 폼과 동일한 별도 입력을 사용한다.
        if (createdBy == null || !createdBy.matches("[1-9][0-9]{0,18}"))
            throw invalid("createdBy", "등록할 사용자를 선택해주세요.");
        
        try { 
        	eventVO.setCreatedBy(Long.valueOf(createdBy)); 
    	}
        
        catch (NumberFormatException exception) { 
        	throw invalid("createdBy", "등록자 ID를 확인해주세요."); 
    	}
        
        BindingResult errors = new org.springframework.validation.BeanPropertyBindingResult(eventVO, "eventCreateVO");
        validatorFactory.getValidator("eventCreateVO", eventVO, errors).validate();
   
        if (errors.hasErrors()) throw new BindException(errors);
        
        eventService.insertEvent(eventVO);
        return Collections.singletonMap("eid",eventVO.getEid());
    }
    
    // 엑셀 다운로드용 코드 목록과 표시명
    private Map<String, List<String>> loadExcelOptions(Definition template) throws Exception {
        Set<String> keys = new LinkedHashSet<>();
        for (SheetMapping sheet : template.getSheets()) for (Mapping item : sheet.getMappings()) {
            if (item instanceof CellMapping && !((CellMapping) item).getDropdown().isEmpty()) keys.add(((CellMapping) item).getDropdown());
        }
        Map<String, List<String>> result = new LinkedHashMap<>();
        for (String key : keys) {
            Set<String> values = new LinkedHashSet<>();
            if ("REGION".equals(key)) {
                values.addAll(regionLabels(selectableRegions()).values());
            } else {
                CodeFilterVO filter = new CodeFilterVO(); filter.setParentCode(key);
                values.addAll(commonLabels(codeService.selectCmmCodeDetail(filter)).values());
            }
            if (values.isEmpty()) throw new IOException("사용 가능한 코드가 없습니다: " + key);
            result.put(key, new ArrayList<>(values));
        }
        return result;
    }

    private String excelCommonLabel(String group, String code) throws Exception {
        CodeFilterVO filter = new CodeFilterVO(); filter.setParentCode(group);
        String label = commonLabels(codeService.selectCmmCodeDetail(filter)).get(code);
        if (label == null) throw new IOException("다운로드할 이벤트의 공통 코드를 찾을 수 없습니다: " + group);
        return label;
    }

    private String excelRegionLabel(Long id) throws Exception {
        List<RegFilterVO> available = selectableRegions();
        Map<String, String> labels = regionLabels(available);
        for (RegFilterVO region : available) if (Objects.equals(id, region.getReg_id()) && labels.containsKey(region.getCode()))
            return labels.get(region.getCode());
        throw new IOException("다운로드할 이벤트의 지역을 찾을 수 없습니다.");
    }

    private static Map<String, String> commonLabels(List<CodeFilterVO> entries) throws IOException {
        Map<String, String> labels = new LinkedHashMap<>();
        for (CodeFilterVO code : entries) addLabel(labels, code.getCode(), code.getName());
        return distinguish(labels);
    }

    private static Map<String, String> regionLabels(List<RegFilterVO> entries) throws IOException {
        Map<Long, String> names = new HashMap<>();
        for (RegFilterVO region : entries) names.put(region.getReg_id(), region.getName());
        Map<String, String> labels = new LinkedHashMap<>();
        for (RegFilterVO region : entries) {
            String parent = names.get(region.getParentRegId());
            if (parent == null) parent = region.getParentName();
            String label = parent == null || parent.trim().isEmpty() ? region.getName() : parent + " " + region.getName();
            addLabel(labels, region.getCode(), label);
        }
        return distinguish(labels);
    }

    private static void addLabel(Map<String, String> labels, String code, String name) throws IOException {
        if (code == null || code.trim().isEmpty()) return;
        if (labels.containsKey(code)) throw new IOException("중복된 코드가 있습니다: " + code);
        labels.put(code, name == null || name.trim().isEmpty() ? code : name.trim());
    }

    private static Map<String, String> distinguish(Map<String, String> labels) throws IOException {
        Map<String, Integer> counts = new HashMap<>();
        for (String label : labels.values()) counts.put(label, counts.getOrDefault(label, 0) + 1);
        Map<String, String> result = new LinkedHashMap<>();
        Set<String> unique = new HashSet<>();
        for (Map.Entry<String, String> entry : labels.entrySet()) {
            String label = counts.get(entry.getValue()) > 1 ? entry.getValue() + " [" + entry.getKey() + "]" : entry.getValue();
            if (!unique.add(label)) throw new IOException("표시명이 중복되어 구분할 수 없습니다: " + label);
            result.put(entry.getKey(), label);
        }
        return result;
    }

    private List<RegFilterVO> selectableRegions() throws Exception {
        Set<Long> roots = new HashSet<>();
        for (RegFilterVO region : regService.selectRegRoot()) roots.add(region.getReg_id());
        List<RegFilterVO> result = new ArrayList<>();
        for (RegFilterVO region : regService.selectRegParents()) {
            if (roots.contains(region.getReg_id()) || roots.contains(region.getParentRegId())) result.add(region);
        }
        return result;
    }

    // 일단 validation check 여기서...
    private EventVO parseExcelEvent(Map<String, Object> mapped, Map<String, Object> preview) throws Exception {
        Object nested = mapped.get("event");
        if (!(nested instanceof Map)) throw invalid("files", "이벤트 엑셀 양식을 확인해주세요.");
        Map<?, ?> source = (Map<?, ?>) nested;
        EventVO event = new EventVO();
        event.setTitle(excelText(source.get("title"), "title", 300, true));
        event.setDsc(excelText(source.get("dsc"), "dsc", 1000, true));
        event.setAddress(excelText(source.get("address"), "address", 500, false));
        event.setCtg(excelCode(source.get("categoryName"), "EVENT_CTG_ROOT", "ctg"));
        event.setStatus(excelCode(source.get("statusName"), "EVENT_STAT_ROOT", "status"));
        event.setSDate(excelDate(source.get("SDate"), "sDate"));
        event.setEDate(excelDate(source.get("EDate"), "eDate"));
        if (event.getEDate().before(event.getSDate())) throw invalid("eDate", "종료일은 시작일 이후여야 합니다.");
        String name = excelText(source.get("regName"), "reg_id", 300, true);
        List<RegFilterVO> matches = new ArrayList<>();
        List<RegFilterVO> options = regService.selectRegParents();
        Set<Long> roots = new HashSet<>();
        for (RegFilterVO region : regService.selectRegRoot()) roots.add(region.getReg_id());
        List<RegFilterVO> selectable = new ArrayList<>();
        for (RegFilterVO region : options) if (roots.contains(region.getReg_id()) || roots.contains(region.getParentRegId())) selectable.add(region);
        Map<String, String> regionLabels = regionLabels(selectable);
        // 현재 폼에서 선택 가능한 시도/시군구만 허용한다. 같은 이름은 상위 지역명으로 구분한다.
        for (RegFilterVO region : options) {
            if (!roots.contains(region.getReg_id()) && !roots.contains(region.getParentRegId())) continue;
            String qualified = region.getParentName() == null ? region.getName()
                    : region.getParentName() + " " + region.getName();
            if (name.equals(String.valueOf(region.getReg_id())) || name.equals(region.getCode())
                    || name.equals(region.getName()) || name.equals(qualified) || name.equals(regionLabels.get(region.getCode()))) matches.add(region);
        }
        if (matches.size() != 1) throw invalid("reg_id", "이벤트 지역을 확인해주세요. 중복 지역명은 '시도 시군구'로 입력해주세요.");
        RegFilterVO region = matches.get(0);
        event.setReg_id(region.getReg_id());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("title", event.getTitle()); result.put("ctg", event.getCtg());
        result.put("status", event.getStatus()); result.put("dsc", event.getDsc());
        result.put("address", event.getAddress()); result.put("reg_id", event.getReg_id());
        result.put("parentRegId", roots.contains(event.getReg_id()) ? event.getReg_id() : region.getParentRegId());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ROOT);
        result.put("sDate", format.format(event.getSDate()));
        result.put("eDate", format.format(event.getEDate()));
        preview.putAll(result);
        return event;
    }

    private String excelCode(Object raw, String parent, String field) throws Exception {
        String value = excelText(raw, field, 300, true);
        CodeFilterVO filter = new CodeFilterVO(); filter.setParentCode(parent);
        List<String> matches = new ArrayList<>();
        List<CodeFilterVO> entries = codeService.selectCmmCodeDetail(filter);
        Map<String, String> labels = commonLabels(entries);
        for (CodeFilterVO code : entries) {
            if (value.equals(code.getCode()) || value.equals(code.getName()) || value.equals(labels.get(code.getCode()))) matches.add(code.getCode());
        }
        if (matches.size() != 1) throw invalid(field, "이벤트 카테고리/상태의 코드 또는 이름을 확인해주세요.");
        return matches.get(0);
    }

    private String excelText(Object raw, String field, int max, boolean required) {
        if (raw != null && !(raw instanceof String)) throw invalid(field, "문자 형식으로 입력해주세요: " + field);
        String value = raw == null ? "" : ((String) raw).trim();
        if ((required && value.isEmpty()) || value.length() > max)
            throw invalid(field, "필수값과 최대 " + max + "자 제한을 확인해주세요: " + field);
        return value;
    }

    private Date excelDate(Object raw, String field) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ROOT);
        format.setLenient(false);
        String value = raw instanceof Date ? format.format((Date) raw) : excelText(raw, field, 16, true);
        if (!value.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}")) throw invalid(field, "날짜는 yyyy-MM-dd HH:mm 형식으로 입력해주세요.");
        ParsePosition position = new ParsePosition(0);
        Date result = format.parse(value, position);
        if (result == null || position.getIndex() != value.length()) throw invalid(field, "날짜 값을 확인해주세요: " + field);
        return result;
    }

    private InvalidEventExcelException invalid(String field, String message) { return new InvalidEventExcelException(field, message); }
    public static class InvalidEventExcelException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        private final String field;
        public InvalidEventExcelException(String field, String message) { super(message); this.field = field; }
        public String getField() { return field; }
    }
}
