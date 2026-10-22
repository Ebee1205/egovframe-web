package com.event.web;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.text.ParsePosition;
import com.reg.service.RegFilterVO;
import com.cmm.service.excel.ExcelTemplate.*;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springmodules.validation.commons.DefaultBeanValidator;
import org.springmodules.validation.commons.DefaultValidatorFactory;

import com.cmm.service.ComResponseVO;
import com.cmm.service.excel.impl.ExcelDomFileParser;
import com.cmm.service.excel.impl.ExcelSaxFileParser;
import com.cmm.service.excel.ExcelFileParser;
import com.cmm.service.file.FileMngService;
import com.cmm.service.file.impl.FileMngUtil;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.ExceptionHandler;
import javax.servlet.http.HttpServletRequest;
import java.util.Locale;
import com.cmm.service.file.FileVO;
import org.springframework.beans.factory.annotation.Value;
import com.code.service.CodeFilterVO;
import com.code.service.CodeService;
import com.reg.service.RegService;

import com.event.service.EventVO;
import com.event.service.EventFilterVO;
import com.event.service.EventService;

import com.event.service.EventCmtVO;
import com.event.service.EventCmtFilterVO;
import com.event.service.EventCmtService;


@Controller
public class EventFileController {

	private final EventService eventService;
	@Autowired
	private ExcelDomFileParser excelFileParser;
	@Autowired
	private ExcelSaxFileParser excelSaxFileParser;
	@Autowired
	private com.cmm.service.excel.ExcelFileWriter excelFileWriter;
	private final EventCmtService eventCmtService;
	private final CodeService codeService;
	private final RegService regService;
	private final FileMngService fileMngService;
	private final MessageSource messageSource;
	@Value("${Globals.fileUpload.Extensions:}")
	private String fileExtensions;
	@Value("${Globals.fileUpload.MaxSizePerFile:52428800}")
	private long fileMaxSize;
	@Autowired
	private DefaultBeanValidator beanValidator;
	@Autowired
	private DefaultValidatorFactory validatorFactory;

	public EventFileController(
		EventService eventService, EventCmtService eventCmtService,  
		CodeService codeService, FileMngService fileMngService, RegService regService, 
		MessageSource messageSource
	) {
		this.eventService = eventService;
		this.eventCmtService = eventCmtService;
		this.codeService = codeService;
		this.fileMngService = fileMngService;
		this.regService = regService;
		this.messageSource = messageSource;
	}

	/** 이벤트 첨부 업로드는 업무 서비스에서 event 접두사를 지정한다. */
	@RequestMapping(value = "/event/file/upload.do", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	public ResponseEntity<String> eventFileUpload(@RequestParam(value = "files", required = false) List<MultipartFile> files,
		@RequestParam(value = "fileCn", required = false) String description, HttpServletRequest request) {
		try {
			List<Map<String, Object>> data = new java.util.ArrayList<>();
			for (FileVO file : eventService.uploadEventFiles(files, description)) {
				data.add(FileMngUtil.responseData(file, request.getContextPath()));
			}
			return ResponseEntity.status(201).body(
				ComResponseVO.success(
					data,
					messageSource.getMessage("file.success.upload", null, Locale.KOREAN)).toJson()
				);
		} catch (Exception exception) {
			return FileMngUtil.errorResponse(exception, messageSource);
		}
	}

	/** 파일 크기 제한 등 요청 파싱 단계의 오류도 공통 응답으로 반환한다. */
	@ExceptionHandler(org.springframework.web.multipart.MultipartException.class)
	public ResponseEntity<String> eventFileUploadError(org.springframework.web.multipart.MultipartException exception) {
		return FileMngUtil.errorResponse(exception, messageSource);
	} 

	/** 엑셀 상세 다운로드 - DOM */	
    @RequestMapping(value = "/event/file/detail/dom.do", method = RequestMethod.GET)
    public void eventFileDetailDom(@RequestParam("eid") Long eid, HttpServletResponse response) throws Exception {
        if (eid == null || eid <= 0) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, "이벤트 ID가 올바르지 않습니다.");
        }
        
        EventVO query = new EventVO();
        query.setEid(eid);
        EventVO event = eventService.selectEvent(query);
        
        if (event == null) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "이벤트를 찾을 수 없습니다.");
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
        
        excelFileWriter.downloadXml(response, "이벤트_상세.xlsx", data, "classpath:excel/EventDetail_Excel.xml", new com.event.service.impl.EventExcelStyle(), options);
    }
	
	/** 엑셀 양식 파일을 저장하지 않고 파싱한 결과만 반환한다. */
	@RequestMapping(value = "/event/file/template/upload.do", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	public ResponseEntity<String> eventFileTemplateUpload(@RequestParam(value = "files", required = false) List<MultipartFile> files, @RequestParam(value = "fileCn", required = false) String description) {

		return parseEventExcel(files, description, excelFileParser);
    }

	/** 엑셀 양식 파일을 저장하지 않고 파싱한 결과만 반환한다. */
	@RequestMapping(value = "/event/file/template/insert.do", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	public ResponseEntity<String> eventFileTemplateInsert(@RequestParam(value = "files", required = false) List<MultipartFile> files,
			@RequestParam(value = "fileCn", required = false) String description) {
		return parseEventExcel(files, description, excelFileParser);
	}

    private ResponseEntity<String> parseEventExcel(List<MultipartFile> files, String description, ExcelFileParser parser) {
		try {
			if (files == null || files.size() != 1) throw FileMngUtil.bad("FILE_COUNT_INVALID", "files");
            List<Map<String, Object>> data = parser.parseXml(files, description, "classpath:excel/EventDetail_Excel.xml");
            @SuppressWarnings("unchecked")
            Map<String, Object> content = (Map<String, Object>) data.get(0).get("fileContent");
            @SuppressWarnings("unchecked")
            Map<String, Object> mapped = (Map<String, Object>) content.get("data");
            content.put("event", parseExcelEvent(mapped));
			
			// 미리보기만 반환한다. 이벤트/첨부 저장 서비스는 호출하지 않는다.
			return ResponseEntity.ok(ComResponseVO.success(data,
					messageSource.getMessage("success.request.msg", null, Locale.KOREAN)).toJson());
		} catch (InvalidEventExcelException exception) {
            return ResponseEntity.badRequest().body(ComResponseVO.fieldFailure("EVENT_EXCEL_INVALID",
                    exception.getMessage(), exception.getField(), "EVENT_EXCEL_INVALID", exception.getMessage()).toJson());
        } catch (Exception exception) {
            return FileMngUtil.errorResponse(exception, messageSource);
        }
    }

    @RequestMapping(value = "/event/file/template/download.do", method = RequestMethod.GET)
    public void eventFileTemapltedownload(HttpServletResponse response) throws Exception {
        String resource = "classpath:excel/EventDetail_Excel.xml";
        com.cmm.service.excel.ExcelTemplate.Definition template = new com.cmm.service.excel.ExcelTemplate().load(resource);
        excelFileWriter.downloadXml(response, "이벤트_등록_양식.xlsx", null, resource,
                new com.event.service.impl.EventExcelStyle(), loadExcelOptions(template));
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

    // 신규 등록 미리보기: 허용 필드 변환 및 검증
    private Map<String, Object> parseExcelEvent(Map<String, Object> mapped) throws Exception {
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
        return result;
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
    private static class InvalidEventExcelException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        private final String field;
        public InvalidEventExcelException(String field, String message) { super(message); this.field = field; }
        public String getField() { return field; }
    }
}
