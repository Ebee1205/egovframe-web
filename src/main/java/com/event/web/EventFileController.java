package com.event.web;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ContentDisposition;
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
import com.cmm.service.excel.impl.ExcelFileParserUtil;
import com.cmm.service.file.FileMngService;
import com.cmm.service.file.impl.FileMngUtil;

import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
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
	private ExcelFileParserUtil excelFileParser;
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

	/** 엑셀 양식 파일을 저장하지 않고 파싱한 결과만 반환한다. */
	@RequestMapping(value = "/event/file/template/upload.do", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	public ResponseEntity<String> eventFileTemplateUpload(@RequestParam(value = "files", required = false) List<MultipartFile> files,
			@RequestParam(value = "fileCn", required = false) String description) {
		try {
			List<Map<String, Object>> data = excelFileParser.parse(files, description);
			return ResponseEntity.ok(ComResponseVO.success(data,
					messageSource.getMessage("success.request.msg", null, Locale.KOREAN)).toJson());
		} catch (Exception exception) {
			return FileMngUtil.errorResponse(exception, messageSource);
		}
	}

	@RequestMapping(value = "/event/file/template/download.do", method = RequestMethod.GET)
	public void eventFileTemapltedownload(HttpServletResponse response) throws IOException                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  {
		Path template = Paths.get("D:/document/files/template/event_detail_template.xlsx").normalize();
		if (!Files.isRegularFile(template)) {
			throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND,
					"이벤트 엑셀 양식 파일을 찾을 수 없습니다.");
		}

		try (InputStream input = Files.newInputStream(template)) {
			// TODO 템플릿 경로 하드코딩
			response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			response.setContentLengthLong(Files.size(template));
			response.setHeader("Content-Disposition", ContentDisposition.attachment()
			        .filename("이벤트_등록_양식.xlsx", StandardCharsets.UTF_8)
			        .build().toString());
			response.setHeader("X-Content-Type-Options", "nosniff");
			response.setHeader("Cache-Control", "private, no-store");
			StreamUtils.copy(input, response.getOutputStream());
			response.flushBuffer();
		}
	}
	
	
}
