package com.cmm.web;

import org.springframework.stereotype.Controller;
import com.cmm.service.file.FileMngService;

/** Common download controller scaffold. Routes follow storage read and access control implementation. */
@Controller
public class FileDownloadController {
    protected final FileMngService fileMngService;

    public FileDownloadController(FileMngService fileMngService) {
        this.fileMngService = fileMngService;
    }
}