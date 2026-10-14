package com.cmm.web;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.ContentDisposition;
import org.springframework.http.ResponseEntity;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.cmm.service.file.FileMngService;
import com.cmm.service.file.FileVO;
import com.cmm.service.file.impl.FileMngUtil;
import java.util.Locale;

@Controller
public class FileDownloadController {
    private final FileMngService service;
    private final MessageSource messageSource;
    public FileDownloadController(FileMngService service, MessageSource messageSource) {
        this.service = service;
        this.messageSource = messageSource;
    }

    @RequestMapping(value = {"/cmm/file/download.do"}, method = RequestMethod.GET)
    public void download(@RequestParam("fileId") Long fileId, HttpServletResponse response) throws IOException {
        FileVO query = new FileVO(); query.setFileId(fileId);
        FileVO file = service.selectFileInf(query);
        // Open before committing response headers so missing objects still get a JSON error.
        try (InputStream input = service.openFile(file)) {
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", ContentDisposition.attachment()
                    .filename(file.getOrignlFileNm(), StandardCharsets.UTF_8).build().toString());
            response.setHeader("X-Content-Type-Options", "nosniff");
            response.setHeader("Cache-Control", "private, no-store");
            StreamUtils.copy(input, response.getOutputStream());
            response.flushBuffer();
        }
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handle(Exception exception, HttpServletResponse response) throws IOException {
        if (response.isCommitted()) {
            if (exception instanceof IOException) { throw (IOException) exception; }
            throw new IOException("Download failed after response was committed.", exception);
        }
        response.reset();
        return FileMngUtil.errorResponse(exception, messageSource);
    }
}
