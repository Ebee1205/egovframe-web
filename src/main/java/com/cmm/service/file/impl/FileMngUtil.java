package com.cmm.service.file.impl;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;

import com.cmm.service.ComResponseVO;
import com.cmm.service.file.FileVO;

import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Component
public class FileMngUtil {
    /** 생성한 파일을 저장소/DB에 저장하지 않고 내려준다. 호출자가 응답 스트림을 소유한다. */
    public static void download(javax.servlet.http.HttpServletResponse response, String fileName,
            String mediaType, byte[] content) throws IOException {
        if (content == null || fileName == null || fileName.trim().isEmpty()
                || fileName.chars().anyMatch(c -> c < 32 || c == 127)
                || fileName.contains("/") || fileName.contains("\\")) {
            throw new IllegalArgumentException("다운로드 파일명과 내용이 올바르지 않습니다.");
        }
        response.setContentType(mediaType);
        response.setContentLengthLong(content.length);
        response.setHeader("Content-Disposition", org.springframework.http.ContentDisposition.attachment()
                .filename(fileName, java.nio.charset.StandardCharsets.UTF_8).build().toString());
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Cache-Control", "private, no-store");
        response.getOutputStream().write(content);
        response.flushBuffer();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(FileMngUtil.class);
    private final Set<String> extensions;
    private final long maxSize;
    private final int maxFiles;
    private static final String DEFAULT_NAME_FORMAT = "{prefix}/{prefix}_{date:yyyyMMdd}_{uuid}_{num}.{ext}";
    private static final java.util.regex.Pattern NAME_TOKEN = java.util.regex.Pattern.compile("\\{(prefix|uuid|num|ext|date(?::[^{}]+)?)\\}");
    private final String nameFormat;
    private final java.util.List<String> imageExtensions;

    public FileMngUtil(
            @Value("${Globals.fileUpload.Extensions:}") String allowedExtensions,
            @Value("${Globals.fileUpload.Extensions.Images:}") String imageExtensions,
            @Value("${Globals.fileUpload.MaxSizePerFile:52428800}") long maxSize,
            @Value("${Globals.fileUpload.MaxFiles:20}") int maxFiles,
            @Value("${Globals.fileUpload.NameFormat:}") String nameFormat) {

        this.extensions = new HashSet<>(Arrays.asList(allowedExtensions.toLowerCase(Locale.ROOT).split("\\.")));
        this.extensions.remove("");
        java.util.List<String> images = new java.util.ArrayList<>(Arrays.asList(imageExtensions.toLowerCase(Locale.ROOT).split("\\.")));
        images.removeIf(String::isEmpty);

        this.imageExtensions = java.util.Collections.unmodifiableList(images);
        this.maxSize = maxSize;
        this.maxFiles = maxFiles;

        if (maxSize <= 0 || maxFiles <= 0) {
            throw new IllegalArgumentException("Invalid file upload configuration.");
        }

        this.nameFormat = nameFormat == null || nameFormat.trim().isEmpty() ? DEFAULT_NAME_FORMAT : nameFormat.trim();
        if (!this.nameFormat.contains("{prefix}") || !this.nameFormat.contains("{uuid}")
                || !this.nameFormat.endsWith(".{ext}")) {
            throw new IllegalArgumentException("Globals.fileUpload.NameFormat requires {prefix}, {uuid}, and a .{ext} suffix.");
        }
        
        // Validate tokens and date patterns during initialization, before any upload is written.
        formatFileName("file", "png", 1);
    }

    /**
     * 서비스에서 지정한 접두사와 전역 파일명 형식으로 저장 경로와 파일명을 생성한다.
     * {prefix}, {uuid}, {num}, {ext}, {date}, {date:날짜패턴}을 지원한다.
     *
     * @param prefix 업무 서비스에서 지정한 파일명 접두사
     * @param extension 점을 제외한 파일 확장자
     * @param number 이번 업로드 요청 안에서의 파일 순번. 1부터 시작한다.
     * @return 저장소 기준의 상대 경로와 파일명
     */
    public String formatFileName(String prefix, String extension, int number) {
        if (prefix == null || !prefix.matches("[A-Za-z0-9_-]{1,100}")) {
            throw new IllegalArgumentException("File prefix must be 1~100 letters, digits, underscores or hyphens.");
        }
        if (extension == null || !extension.matches("[a-z0-9]{1,20}") || number < 1) {
            throw new IllegalArgumentException("Invalid file extension or sequence number.");
        }
        String uuid = UUID.randomUUID().toString().replace("-", "");
        ZonedDateTime now = ZonedDateTime.now();
        java.util.regex.Matcher matcher = NAME_TOKEN.matcher(nameFormat);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String token = matcher.group(1);
            String value;
            if ("prefix".equals(token)) { value = prefix; }
            else if ("uuid".equals(token)) { value = uuid; }
            else if ("num".equals(token)) { value = Integer.toString(number); }
            else if ("ext".equals(token)) { value = extension; }
            else {
                String pattern = "date".equals(token) ? "yyyyMMdd" : token.substring(5);
                try { value = now.format(DateTimeFormatter.ofPattern(pattern, Locale.ROOT)); }
                catch (IllegalArgumentException exception) {
                    throw new IllegalArgumentException("Invalid date pattern in Globals.fileUpload.NameFormat.", exception);
                }
            }
            matcher.appendReplacement(result, java.util.regex.Matcher.quoteReplacement(value));
        }
        matcher.appendTail(result);
        String name = result.toString();
        if (name.length() > 255 || !name.matches("[A-Za-z0-9_-]+(?:[./][A-Za-z0-9_-]+)*")) {
            throw new IllegalArgumentException("Globals.fileUpload.NameFormat must produce a safe relative storage path of at most 255 characters.");
        }
        return name;
    }

    public java.util.List<String> imageExtensions() { 
        return imageExtensions; 
    }

    public void validateCount(int count) {
        if (count < 1 || count > maxFiles) { throw bad("FILE_COUNT_INVALID", "files"); }
    }

    public FileVO validate(MultipartFile upload) {
        return validate(upload, maxSize);
    }

    /** 별도 업로드 정책이 있는 업무에서만 사용한다. -1은 용량 제한 없음이다. */
    public FileVO validate(MultipartFile upload, long maxFileSize) {
        if (maxFileSize < -1 || maxFileSize == 0) throw new IllegalArgumentException("Invalid upload size limit.");
        if (upload == null || upload.isEmpty()) { throw bad("FILE_EMPTY", "files"); }
        String name = upload.getOriginalFilename();

        if (name == null) { throw bad("FILE_NAME_INVALID", "files"); }

        name = name.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1).trim();

        if (name.isEmpty() || name.length() > 255 || name.chars().anyMatch(c -> c < 32 || c == 127)) {
            throw bad("FILE_NAME_INVALID", "files");
        }
        
        int dot = name.lastIndexOf('.');
        String extension = dot > 0 ? name.substring(dot + 1).toLowerCase(Locale.ROOT) : "";
        
        if (!extensions.contains(extension) || extension.length() > 20) {
            throw bad("FILE_EXTENSION_NOT_ALLOWED", "files");
        }
        
        if (maxFileSize >= 0 && upload.getSize() > maxFileSize) { throw new FileException(413, "FILE_TOO_LARGE", "files"); }
        
        FileVO file = new FileVO();
        file.setOrignlFileNm(name);
        file.setFileExtsn(extension);
        file.setFileSize(upload.getSize());
        
        return file;
    }

    public FileVO describe(MultipartFile upload, String location, String description, String prefix, int number) {
        FileVO file = validate(upload);
        file.setFileCn(description);
        file.setFileStreCours(location);
        file.setStreFileNm(formatFileName(prefix, file.getFileExtsn(), number));
        file.setCDate(new Date());
        return file;
    }

    public static Long id(Long id) {
        if (id == null || id <= 0) { throw bad("FILE_ID_INVALID", "fileId"); }
        return id;
    }

    public static FileException bad(String code, String field) {
        return new FileException(400, code, field);
    }

    public static Map<String, Object> responseData(FileVO file, String contextPath) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("fileId", file.getFileId());
        data.put("originalName", file.getOrignlFileNm());
        data.put("extension", file.getFileExtsn());
        data.put("fileSize", file.getFileSize());
        data.put("description", file.getFileCn());
        data.put("createdAt", file.getCDate());
        data.put("downloadUrl", contextPath + "/cmm/file/download.do?fileId=" + file.getFileId());
        return data;
    }

    public static String contentType(String extension) {
        if ("png".equals(extension)) { return "image/png"; }
        if ("jpg".equals(extension) || "jpeg".equals(extension)) { return "image/jpeg"; }
        if ("gif".equals(extension)) { return "image/gif"; }
        if ("pdf".equals(extension)) { return "application/pdf"; }
        if ("xls".equals(extension)) { return "application/vnd.ms-excel"; }
        if ("xlsx".equals(extension)) { return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"; }
        return "application/octet-stream";
    }

    public static ResponseEntity<String> errorResponse(Exception exception, MessageSource messageSource) {
        int status = 500;
        String code = "FILE_PROCESSING_FAILED";
        String field = null;
        if (exception instanceof FileException) {
            FileException failure = (FileException) exception;
            status = failure.status; code = failure.code; field = failure.field;
        } else if (exception instanceof MaxUploadSizeExceededException) {
            status = 413; code = "FILE_TOO_LARGE";
        } else if (exception instanceof MultipartException) {
            status = 400; code = "MULTIPART_INVALID";
        } else if (exception instanceof DataIntegrityViolationException) {
            status = 409; code = "FILE_IN_USE";
        } else if (exception instanceof NoSuchFileException
                || (exception instanceof S3Exception && ((S3Exception) exception).statusCode() == 404)) {
            status = 404; code = "FILE_NOT_FOUND";
        } else if (exception instanceof IOException || exception instanceof SdkException) {
            status = 503; code = "FILE_STORAGE_UNAVAILABLE";
        } else if (exception instanceof org.springframework.web.bind.MissingServletRequestParameterException
                || exception instanceof org.springframework.web.multipart.support.MissingServletRequestPartException
                || exception instanceof org.springframework.web.method.annotation.MethodArgumentTypeMismatchException) {
            status = 400; code = "FILE_REQUEST_INVALID";
        }
        String messageKey = exception instanceof MaxUploadSizeExceededException
                ? "file.error.UPLOAD_TOO_LARGE" : "file.error." + code;
        String message = messageSource.getMessage(messageKey, null,
                "파일 처리 중 오류가 발생했습니다.", Locale.KOREAN);
        if (status >= 500) { LOGGER.error("File request failed", exception); }
        ComResponseVO<Void> body = field == null ? ComResponseVO.fail(code, message)
                : ComResponseVO.fieldFailure(code, message, field, code, message);
        return ResponseEntity.status(status).contentType(org.springframework.http.MediaType.APPLICATION_JSON).body(body.toJson());
    }

    public static final class FileException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        private final int status;
        private final String code;
        private final String field;
        public FileException(int status, String code, String field) {
            this(status, code, field, null);
        }
        public FileException(int status, String code, String field, Throwable cause) {
            this(status, code, field, code, cause);
        }
        public FileException(int status, String code, String field, String message, Throwable cause) {
            super(message, cause); this.status = status; this.code = code; this.field = field;
        }
    }
}
