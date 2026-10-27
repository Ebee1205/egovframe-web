package com.extra.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import com.cmm.service.ComResponseVO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMethod;
import java.util.List;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartException;
import com.extra.service.impl.ExtraFileServiceImpl;
import com.cmm.service.file.impl.FileMngUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import com.extra.service.ExtraFilterVO;
import com.extra.service.ExtraService;

@Controller
public class ExtraController {
    private final ExtraFileServiceImpl extraFileService;
    private final MessageSource messageSource;
    private final ExtraService extraService;

    public ExtraController(ExtraFileServiceImpl extraFileService, MessageSource messageSource, ExtraService extraService) {
        this.extraFileService = extraFileService;
        this.messageSource = messageSource;
        this.extraService = extraService;
    }

    @RequestMapping("/extra/list.do")
    public String extraList(ExtraFilterVO filterVO, Model model) throws Exception {
    	
        model.addAttribute("extras", extraService.selectExtraList(filterVO));
        model.addAttribute("filterVO", filterVO);
        model.addAttribute("totalPage", 
        		filterVO.getPageSize() <= 0 || filterVO.getTotalCnt() == 0 ? 0
        		: (filterVO.getTotalCnt() - 1) / filterVO.getPageSize() + 1);
        return "forward:/WEB-INF/jsp/extra/ExtraList.jsp";
    }
    
   
    @RequestMapping(value = "/extra/file/upload.do", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public ResponseEntity<String> extraFileUpload(@RequestParam(value = "files", required = false) List<MultipartFile> files) {
    	
        try {
            if (files == null || files.size() != 1) throw FileMngUtil.bad("FILE_COUNT_INVALID", "files");
            
            return ResponseEntity.ok(
            	ComResponseVO.success(
            		extraFileService.parseExtraExcel(files),
                    messageSource.getMessage("success.request.msg", null, Locale.KOREAN)
                ).toJson());
            
        } catch (ExtraFileServiceImpl.InvalidExtraExcelException exception) {
            return ResponseEntity.badRequest().body(ComResponseVO.fieldFailure("EXTRA_EXCEL_PARSE_FAILED", exception.getMessage(), "files", "EXTRA_EXCEL_PARSE_FAILED", exception.getMessage()).toJson());
        } catch (Exception exception) {
            return FileMngUtil.errorResponse(exception, messageSource);
        }
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<String> extraFileUploadError(MultipartException exception) {
        return FileMngUtil.errorResponse(exception, messageSource);
    }
   
    // 오류 목록 다운로드
    @RequestMapping("/extra/file/err/download.do")
    public String extraFileErrDownload(Model model) throws Exception {
    	
    	
        return "forward:/WEB-INF/jsp/extra/ExtraList.jsp";
    }
    

}
