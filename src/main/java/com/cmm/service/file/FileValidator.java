package com.cmm.service.file;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Validates file metadata independently of HTTP multipart and storage providers. */
@Component
public class FileValidator {
    private final Set<String> allowedExtensions;

    public FileValidator(@Value("${Globals.fileUpload.Extensions:}") String extensions) {
        allowedExtensions = new HashSet<>(Arrays.asList(extensions.toLowerCase(Locale.ROOT).split("\\.")));
        allowedExtensions.remove("");
    }

    public void validate(String originalName, long size, long maxSize) {
        if (originalName == null || originalName.trim().isEmpty()) {
            throw new IllegalArgumentException("File name is required.");
        }
        if (size <= 0 || (maxSize >= 0 && size > maxSize)) {
            throw new IllegalArgumentException("Invalid file size.");
        }
        String name = originalName.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1);
        int dot = name.lastIndexOf('.');
        if (dot <= 0 || !allowedExtensions.contains(name.substring(dot + 1).toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("File extension is not allowed.");
        }
    }
}