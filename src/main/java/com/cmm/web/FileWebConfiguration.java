package com.cmm.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

@Configuration
public class FileWebConfiguration {
    @Bean(name = "multipartResolver")
    public CommonsMultipartResolver multipartResolver(
            @Value("${Globals.fileUpload.MaxSize:-1}") long maxUploadSize) {
        CommonsMultipartResolver resolver = new CommonsMultipartResolver();
        resolver.setDefaultEncoding("UTF-8");
        resolver.setResolveLazily(true);
        resolver.setMaxUploadSize(maxUploadSize);
        return resolver;
    }
}