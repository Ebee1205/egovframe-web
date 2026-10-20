package com.cmm.service.excel;

import java.io.IOException;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

public interface ExcelFileParser {

    Map<String, Object> parseMapped(MultipartFile file, ExcelTemplate.Definition template) throws IOException;

    Map<String, Object> parseXml(MultipartFile file, String classpathXml) throws IOException;

}
