package com.extra.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

public interface ExtraFileService {
    Map<String, Object> parseExtraExcel(List<MultipartFile> files);
    Map<String, Object> insertExtraExcel(List<MultipartFile> files);
}
