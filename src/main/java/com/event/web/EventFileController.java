package com.event.web;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.http.HttpServletResponse;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.cmm.service.ComResponseVO;
import com.cmm.service.file.impl.FileMngUtil;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.ExceptionHandler;
import javax.servlet.http.HttpServletRequest;
import java.util.Locale;
import com.cmm.service.file.FileVO;

import com.event.service.EventFilterVO;
import com.event.service.EventService;



@Controller
public class EventFileController {

    private final EventService eventService;
    private final MessageSource messageSource;
    private final com.cmm.service.excel.impl.ExcelFileServiceImpl excelFileService;
    public EventFileController(EventService eventService, MessageSource messageSource,
            com.cmm.service.excel.impl.ExcelFileServiceImpl excelFileService) {
        this.eventService=eventService; this.messageSource=messageSource; this.excelFileService=excelFileService;
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




    /** 검색 조건에 맞는 전체 목록을 SXSSF로 다운로드한다. */
    @RequestMapping(value = {"/event/file/list/download.do", "/event/file/list/download"}, method = RequestMethod.GET)
    public void eventFileListDownload(EventFilterVO filterVO, HttpServletResponse response) throws Exception {
        String fileName = "이벤트목록_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".xlsx";
        response.setContentType(FileMngUtil.contentType("xlsx"));
        response.setHeader("Content-Disposition", org.springframework.http.ContentDisposition.attachment()
                .filename(fileName, java.nio.charset.StandardCharsets.UTF_8).build().toString());
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Cache-Control", "private, no-store");

        excelFileService.writeEventList(filterVO, response.getOutputStream());
        response.flushBuffer();
	}

	/** 엑셀 상세 다운로드 */
    @RequestMapping(value = "/event/file/detail/dom.do", method = RequestMethod.GET)
    public void eventFileDetailDom(@RequestParam("eid") Long eid, HttpServletResponse response) throws Exception {
        FileMngUtil.download(response, "이벤트_상세.xlsx", FileMngUtil.contentType("xlsx"), excelFileService.writeEventDetail(eid));
    }

	/** 엑셀 양식 파일 저장X / 파싱한 결과만 반환 */
	@RequestMapping(value = "/event/file/template/upload.do", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	public ResponseEntity<String> eventFileTemplateUpload(@RequestParam(value = "files", required = false) List<MultipartFile> files,
			@RequestParam(value = "fileCn", required = false) String description) {

		return excelResponse(files, description, false, null);
    }

    /** 엑셀 양식 파일 저장X / 파싱한 결과 이벤트 등록 */
    @RequestMapping(value = {"/event/file/template/insert.do", "/event/template/insert.do"}, method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public ResponseEntity<String> eventFileTemplateInsert(@RequestParam(value = "files", required = false) List<MultipartFile> files,
            @RequestParam(value = "fileCn", required = false) String description,
            @RequestParam(value = "createdBy", required = false) String createdBy) {
        return excelResponse(files, description, true, createdBy);
    }

    private ResponseEntity<String> excelResponse(List<MultipartFile> files, String description, boolean insert, String createdBy) {
        try {
            if (insert) return ResponseEntity.status(201).body(ComResponseVO.success(
                    excelFileService.insertEventExcel(files, description, createdBy), "이벤트가 등록되었습니다.").toJson());
            return ResponseEntity.ok(ComResponseVO.success(excelFileService.parseEventExcel(files, description),
                    messageSource.getMessage("success.request.msg", null, Locale.KOREAN)).toJson());
        } catch (BindException exception) {
            return ResponseEntity.badRequest().body(ComResponseVO.validationFailure(exception.getBindingResult(), messageSource,
                    Locale.KOREAN, "EVENT_EXCEL_INVALID", "등록할 이벤트 정보를 확인해주세요.").toJson());
        } catch (com.cmm.service.excel.impl.ExcelFileServiceImpl.InvalidEventExcelException exception) {
            return ResponseEntity.badRequest().body(ComResponseVO.fieldFailure("EVENT_EXCEL_INVALID", exception.getMessage(),
                    exception.getField(), "EVENT_EXCEL_INVALID", exception.getMessage()).toJson());
        } catch (Exception exception) {
            return FileMngUtil.errorResponse(exception, messageSource);
        }
    }

    @RequestMapping(value = "/event/file/template/download.do", method = RequestMethod.GET)
    public void eventFileTemapltedownload(HttpServletResponse response) throws Exception {
        FileMngUtil.download(response, "이벤트_등록_양식.xlsx", FileMngUtil.contentType("xlsx"), excelFileService.writeEventTemplate());
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public void invalidExcelRequest(IllegalArgumentException exception, HttpServletResponse response) throws IOException {
        response.sendError(400, exception.getMessage());
    }
    @ExceptionHandler(java.util.NoSuchElementException.class)
    public void missingExcelEvent(java.util.NoSuchElementException exception, HttpServletResponse response) throws IOException {
        response.sendError(404, exception.getMessage());
    }
}
