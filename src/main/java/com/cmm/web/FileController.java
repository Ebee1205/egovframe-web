package com.cmm.web;

import org.springframework.stereotype.Controller;
import com.cmm.service.file.FileMngService;
import com.cmm.service.file.FileValidator;

/** Common upload/update/delete controller scaffold. Routes follow persistence implementation. */
@Controller
public class FileController {
    protected final FileMngService fileMngService;
    protected final FileValidator fileValidator;

    public FileController(FileMngService fileMngService, FileValidator fileValidator) {
        this.fileMngService = fileMngService;
        this.fileValidator = fileValidator;
    }
}