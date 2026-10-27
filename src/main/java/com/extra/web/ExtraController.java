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
import com.cmm.service.excel.impl.ExcelSaxFileParser;
import com.cmm.service.file.impl.FileMngUtil;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ExtraController {
    private final ExcelSaxFileParser excelFileParser;
    private final MessageSource messageSource;

    public ExtraController(ExcelSaxFileParser excelFileParser, MessageSource messageSource) {
        this.excelFileParser = excelFileParser;
        this.messageSource = messageSource;
    }

    @RequestMapping("/extra/list.do")
    public String extraList(Model model) throws Exception {
    	

    	
        return "forward:/WEB-INF/jsp/extra/ExtraList.jsp";
    }
    
    
    /** 파일 저장이나 업무 매핑 없이 SAX 파싱 결과를 그대로 반환한다. */
    @RequestMapping(value = "/extra/file/upload.do", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public ResponseEntity<String> extraFileUpload(@RequestParam(value = "files", required = false) List<MultipartFile> files) {
    	
        try {
            if (files == null || files.size() != 1) throw FileMngUtil.bad("FILE_COUNT_INVALID", "files");
            return ResponseEntity.ok(ComResponseVO.success(excelFileParser.parse(files, null),
                    messageSource.getMessage("success.request.msg", null, Locale.KOREAN)).toJson());
        } catch (Exception exception) {
            return FileMngUtil.errorResponse(exception, messageSource);
        }
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<String> extraFileUploadError(MultipartException exception) {
        return FileMngUtil.errorResponse(exception, messageSource);
    }
    
}
