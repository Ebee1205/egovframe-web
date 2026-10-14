package com.cmm.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.cmm.service.ComResponseVO;
import com.cmm.service.file.FileMngService;
import com.cmm.service.file.FileVO;
import com.cmm.service.file.impl.FileMngUtil;

@Controller
@RequestMapping("/cmm/file")
public class FileController {
    private static final String JSON = "application/json;charset=UTF-8";
    private final FileMngService service;
    private final MessageSource messageSource;
    public FileController(FileMngService service, MessageSource messageSource) {
        this.service = service;
        this.messageSource = messageSource;
    }

    @RequestMapping(value = "/upload.do", method = RequestMethod.POST, produces = JSON)
    public ResponseEntity<String> upload(@RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "fileCn", required = false) String description, HttpServletRequest request) throws IOException {
        List<Map<String, Object>> data = new ArrayList<>();
        for (FileVO file : service.uploadFiles(files, description)) { data.add(FileMngUtil.responseData(file, request.getContextPath())); }
        return ResponseEntity.status(201).body(ComResponseVO.success(data,
                messageSource.getMessage("file.success.upload", null, Locale.KOREAN)).toJson());
    }
    @RequestMapping(value = "/list.do", method = RequestMethod.GET, produces = JSON)
    public ResponseEntity<String> list(@RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "imagesOnly", defaultValue = "false") boolean imagesOnly,
            HttpServletRequest request) {
        FileVO filter = new FileVO();
        filter.setOrignlFileNm(keyword); filter.setImagesOnly(imagesOnly);
        List<Map<String, Object>> items = new ArrayList<>();
        for (FileVO file : service.selectFileInfs(filter)) { items.add(FileMngUtil.responseData(file, request.getContextPath())); }
        Map<String, Object> data = new LinkedHashMap<>(); data.put("items", items);
        return ResponseEntity.ok(ComResponseVO.success(data, null).toJson());
    }
    @RequestMapping(value = "/detail.do", method = RequestMethod.GET, produces = JSON)
    public ResponseEntity<String> detail(@RequestParam("fileId") Long fileId, HttpServletRequest request) {
        FileVO query = new FileVO(); query.setFileId(fileId);
        return ResponseEntity.ok(ComResponseVO.success(FileMngUtil.responseData(service.selectFileInf(query),
                request.getContextPath()), null).toJson());
    }
    @RequestMapping(value = "/update.do", method = RequestMethod.POST, produces = JSON)
    public ResponseEntity<String> update(@RequestParam("fileId") Long fileId,
            @RequestParam(value = "file", required = false) MultipartFile replacement,
            @RequestParam(value = "fileCn", required = false) String description, HttpServletRequest request) throws IOException {
        FileVO file = service.updateFile(fileId, replacement, description);
        return ResponseEntity.ok(ComResponseVO.success(FileMngUtil.responseData(file, request.getContextPath()),
                messageSource.getMessage("file.success.update", null, Locale.KOREAN)).toJson());
    }
    @RequestMapping(value = "/delete.do", method = {RequestMethod.POST, RequestMethod.DELETE}, produces = JSON)
    public ResponseEntity<String> delete(@RequestParam("fileId") Long fileId) {
        FileVO file = new FileVO(); file.setFileId(fileId); service.deleteFileInf(file);
        return ResponseEntity.ok(ComResponseVO.success(null,
                messageSource.getMessage("file.success.delete", null, Locale.KOREAN)).toJson());
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handle(Exception exception) {
        return FileMngUtil.errorResponse(exception, messageSource);
    }
}
