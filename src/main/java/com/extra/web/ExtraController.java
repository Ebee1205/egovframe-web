package com.extra.web;

import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Value;
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
import com.extra.service.ExtraFileService;
import com.cmm.service.file.impl.FileMngUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import com.extra.service.ExtraFilterVO;
import com.extra.service.ExtraService;

@Controller
public class ExtraController {
    @Value("${Globals.extra.fileUpload.MaxSizePerFile:-1}")
    private long extraFileMaxSize = -1;
    private final ExtraFileService extraFileService;
    private final MessageSource messageSource;
    private final ExtraService extraService;

    public ExtraController(ExtraFileService extraFileService, MessageSource messageSource, ExtraService extraService) {
        this.extraFileService = extraFileService;
        this.messageSource = messageSource;
        this.extraService = extraService;
    }

    @RequestMapping("/extra/list.do")
    public String extraList(ExtraFilterVO filterVO, Model model) throws Exception {
    	
        model.addAttribute("extras", extraService.selectExtraList(filterVO));
        model.addAttribute("filterVO", filterVO);
        model.addAttribute("fileMaxSize", extraFileMaxSize);
        model.addAttribute("totalPage", filterVO.getPageSize() <= 0 || filterVO.getTotalCnt() == 0 ? 0 : (filterVO.getTotalCnt() - 1) / filterVO.getPageSize() + 1);
        
        return "forward:/WEB-INF/jsp/extra/ExtraList.jsp";
    }
       
    @RequestMapping(value = "/extra/file/upload.do", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public ResponseEntity<String> extraFileUpload(@RequestParam(value = "files", required = false) List<MultipartFile> files) {
    	
        return ResponseEntity.ok(
            ComResponseVO.success(
                extraFileService.parseExtraExcel(files),
                messageSource.getMessage("success.request.msg", null, Locale.KOREAN)
            ).toJson());
    }

    @RequestMapping(value = {"/extra/file/insert.do", "/file/insert.do"}, method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public ResponseEntity<String> extraFileInsert(@RequestParam(value = "files", required = false) List<MultipartFile> files) {
        return ResponseEntity.status(201).body(ComResponseVO.success(extraFileService.insertExtraExcel(files), "대여이력이 일괄 등록되었습니다.").toJson());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> extraFileError(Exception exception) {
        return FileMngUtil.errorResponse(exception, messageSource);
    }
   
    // 오류 목록 다운로드
    @RequestMapping("/extra/file/err/download.do")
    public String extraFileErrDownload(Model model) throws Exception {
    	
    	return "forward:/WEB-INF/jsp/extra/ExtraList.jsp";
    }
}
