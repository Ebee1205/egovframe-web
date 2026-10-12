package com.cmm.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

/** DispatcherServlet finds the resolver by its multipartResolver bean name. */
@Component("multipartResolver")
public class MultipartResolver extends CommonsMultipartResolver {
    public MultipartResolver(@Value("${Globals.fileUpload.MaxSize:104857600}") long maxRequestSize,
            @Value("${Globals.fileUpload.MaxSizePerFile:52428800}") long maxFileSize) {
        if (maxRequestSize <= 0 || maxFileSize <= 0 || maxFileSize > maxRequestSize) {
            throw new IllegalArgumentException("Invalid multipart upload size limits.");
        }
        setDefaultEncoding("UTF-8");
        setMaxUploadSize(maxRequestSize);
        setMaxUploadSizePerFile(maxFileSize);
        setMaxInMemorySize(10240);
        // Lazy parsing allows controller-specific error handlers to format size errors.
        setResolveLazily(true);
    }
}
