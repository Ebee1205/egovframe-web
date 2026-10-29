package com.cmm.web;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.util.UrlPathHelper;

/** DispatcherServlet finds the resolver by its multipartResolver bean name. */
@Component("multipartResolver")
public class MultipartResolver extends CommonsMultipartResolver {
    private final CommonsMultipartResolver extraResolver = new CommonsMultipartResolver();

    public MultipartResolver(@Value("${Globals.fileUpload.MaxSize:104857600}") long maxRequestSize,
            @Value("${Globals.fileUpload.MaxSizePerFile:52428800}") long maxFileSize,
            @Value("${Globals.extra.fileUpload.MaxSize:-1}") long extraMaxRequestSize,
            @Value("${Globals.extra.fileUpload.MaxSizePerFile:-1}") long extraMaxFileSize) {
        if (maxRequestSize <= 0 || maxFileSize <= 0 || maxFileSize > maxRequestSize) {
            throw new IllegalArgumentException("Invalid multipart upload size limits.");
        }
        setDefaultEncoding("UTF-8");
        setMaxUploadSize(maxRequestSize);
        setMaxUploadSizePerFile(maxFileSize);
        setMaxInMemorySize(10240);
        // Lazy parsing allows controller-specific error handlers to format size errors.
        setResolveLazily(true);
        if (extraMaxRequestSize < -1 || extraMaxRequestSize == 0
                || extraMaxFileSize < -1 || extraMaxFileSize == 0
                || (extraMaxRequestSize > 0 && (extraMaxFileSize < 0 || extraMaxFileSize > extraMaxRequestSize))) {
            throw new IllegalArgumentException("Invalid extra upload size limits.");
        }
        extraResolver.setDefaultEncoding("UTF-8");
        extraResolver.setMaxUploadSize(extraMaxRequestSize);
        extraResolver.setMaxUploadSizePerFile(extraMaxFileSize);
        extraResolver.setMaxInMemorySize(10240);
        extraResolver.setResolveLazily(true);
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        super.setServletContext(servletContext);
        extraResolver.setServletContext(servletContext);
    }

    @Override
    public MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) {
        String path = UrlPathHelper.defaultInstance.getPathWithinApplication(request);
        if ("/extra/file/upload.do".equals(path) || "/extra/file/insert.do".equals(path)
                || "/file/insert.do".equals(path)) {
            return extraResolver.resolveMultipart(request);
        }
        return super.resolveMultipart(request);
    }
}
