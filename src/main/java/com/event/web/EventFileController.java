package com.event.web;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

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
	private com.event.service.EventExcelPreview eventExcelPreview;
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
        
        String statusName = event.getStatus();
        
        CodeFilterVO filter = new CodeFilterVO();
        filter.setParentCode("EVENT_STAT_ROOT");
        for (CodeFilterVO code : codeService.selectCmmCodeDetail(filter)) {
            if (java.util.Objects.equals(code.getCode(), event.getStatus())) {
                statusName = code.getName();
                break;
            }
        }
        
        Map<String, Object> data = new HashMap<>();
        
        String categoryName = event.getCtg();
        CodeFilterVO categoryFilter = new CodeFilterVO();
        categoryFilter.setParentCode("EVENT_CTG_ROOT");
        for (CodeFilterVO code : codeService.selectCmmCodeDetail(categoryFilter)) {
            if (java.util.Objects.equals(code.getCode(), event.getCtg())) {
                categoryName = code.getName();
                break;
            }
        }
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("title", event.getTitle());
        eventData.put("regName", event.getRegName());
        eventData.put("address", event.getAddress());
        eventData.put("SDate", event.getSDate());
        eventData.put("EDate", event.getEDate());
        eventData.put("dsc", event.getDsc());
        eventData.put("categoryName", categoryName);
        eventData.put("statusName", statusName);
        data.put("event", eventData);
        
        excelFileWriter.downloadXml(response, "이벤트_상세.xlsx", data, "classpath:excel/EventDetail_Excel.xml", new com.event.service.impl.EventExcelStyle());
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
            content.put("event", eventExcelPreview.convert(mapped));
			
			// 미리보기만 반환한다. 이벤트/첨부 저장 서비스는 호출하지 않는다.
			return ResponseEntity.ok(ComResponseVO.success(data,
					messageSource.getMessage("success.request.msg", null, Locale.KOREAN)).toJson());
		} catch (com.event.service.EventExcelPreview.InvalidPreview exception) {
            return ResponseEntity.badRequest().body(ComResponseVO.fieldFailure("EVENT_EXCEL_INVALID",
                    exception.getMessage(), exception.getField(), "EVENT_EXCEL_INVALID", exception.getMessage()).toJson());
        } catch (Exception exception) {
            return FileMngUtil.errorResponse(exception, messageSource);
        }
    }

    @RequestMapping(value = "/event/file/template/download.do", method = RequestMethod.GET)
	public void eventFileTemapltedownload(HttpServletResponse response) throws IOException {
        java.nio.file.Path template = java.nio.file.Paths.get("D:/document/files/template/event_detail_template.xlsx");
        if (!java.nio.file.Files.isRegularFile(template)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND,
                    "이벤트 등록 양식 파일을 찾을 수 없습니다.");
        }
        byte[] content = java.nio.file.Files.readAllBytes(template);
        FileMngUtil.download(response, "이벤트_등록_양식.xlsx", FileMngUtil.contentType("xlsx"), content);
    }
}
