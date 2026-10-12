package com.cmm.web;

import org.springframework.stereotype.Controller;
import com.cmm.service.file.FileMngService;
import com.cmm.service.file.FileValidator;

/** Common upload/update/delete controller scaffold. Routes follow persistence implementation. */
@Controller
public class FileController {
    protected final FileMngService fileMngService;

    public FileController(FileMngService fileMngService) {
        this.fileMngService = fileMngService;
    }
}