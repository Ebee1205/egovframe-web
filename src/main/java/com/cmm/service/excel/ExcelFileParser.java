package com.cmm.service.excel;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public interface ExcelFileParser {

    <T> ExcelFileVO<T> parse(
        MultipartFile file,
        ExcelTemplate<T> template
    ) throws IOException;
}