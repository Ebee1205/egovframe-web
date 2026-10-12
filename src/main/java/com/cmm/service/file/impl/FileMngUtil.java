package com.cmm.service.file.impl;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.time.LocalDate;
import java.time.ZoneOffset;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(FileMngUtil.class);
    private final Set<String> extensions;
    private final long maxSize;
    private final int maxFiles;
    private final String prefix;

    public FileMngUtil(@Value("${Globals.fileUpload.Extensions:}") String allowedExtensions,
            @Value("${Globals.fileUpload.MaxSizePerFile:52428800}") long maxSize,
            @Value("${Globals.fileUpload.MaxFiles:20}") int maxFiles,
            @Value("${Globals.fileUpload.KeyPrefix:files}") String prefix) {
        this.extensions = new HashSet<>(Arrays.asList(allowedExtensions.toLowerCase(Locale.ROOT).split("\\.")));
        this.extensions.remove("");
        this.maxSize = maxSize;
        this.maxFiles = maxFiles;
        if (maxSize <= 0 || maxFiles <= 0 || prefix == null || prefix.length() > 100
                || !prefix.matches("[A-Za-z0-9_-]+(?:/[A-Za-z0-9_-]+)*")) {
            throw new IllegalArgumentException("Invalid file upload configuration.");
        }
        this.prefix = prefix;
    }
    public void validateCount(int count) {
        if (count < 1 || count > maxFiles) { throw bad("FILE_COUNT_INVALID", "업로드할 파일 수를 확인해주세요.", "files"); }
    }
    public FileVO describe(MultipartFile upload, String location, String description) {
        if (upload == null || upload.isEmpty()) { throw bad("FILE_EMPTY", "빈 파일은 업로드할 수 없습니다.", "files"); }
        String name = upload.getOriginalFilename();
        if (name == null) { throw bad("FILE_NAME_INVALID", "파일 이름을 확인해주세요.", "files"); }
        name = name.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1).trim();
        if (name.isEmpty() || name.length() > 255 || name.chars().anyMatch(c -> c < 32 || c == 127)) {
            throw bad("FILE_NAME_INVALID", "파일 이름을 확인해주세요.", "files");
        }
        int dot = name.lastIndexOf('.');
        String extension = dot > 0 ? name.substring(dot + 1).toLowerCase(Locale.ROOT) : "";
        if (!extensions.contains(extension) || extension.length() > 20) {
            throw bad("FILE_EXTENSION_NOT_ALLOWED", "허용되지 않는 파일 확장자입니다.", "files");
        }
        if (upload.getSize() > maxSize) { throw new FileException(413, "FILE_TOO_LARGE", "파일 크기 제한을 초과했습니다.", "files"); }
        FileVO file = new FileVO();
        file.setOrignlFileNm(name);
        file.setFileExtsn(extension);
        file.setFileSize(upload.getSize());
        file.setFileCn(description);
        file.setFileStreCours(location);
        file.setStreFileNm(prefix + "/" + LocalDate.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
                + "/" + UUID.randomUUID().toString().replace("-", "") + "." + extension);
        file.setCDate(new Date());
        return file;
    }
    public static Long id(Long id) {
        if (id == null || id <= 0) { throw bad("FILE_ID_INVALID", "파일 ID를 확인해주세요.", "fileId"); }
        return id;
    }
    public static FileException bad(String code, String message, String field) {
        return new FileException(400, code, message, field);
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
    public static ResponseEntity<String> errorResponse(Exception exception) {
        int status = 500;
        String code = "FILE_PROCESSING_FAILED";
        String message = "파일 처리 중 오류가 발생했습니다.";
        String field = null;
        if (exception instanceof FileException) {
            FileException failure = (FileException) exception;
            status = failure.status; code = failure.code; message = failure.getMessage(); field = failure.field;
        } else if (exception instanceof MaxUploadSizeExceededException) {
            status = 413; code = "FILE_TOO_LARGE"; message = "업로드 크기 제한을 초과했습니다.";
        } else if (exception instanceof MultipartException) {
            status = 400; code = "MULTIPART_INVALID"; message = "파일 업로드 요청을 확인해주세요.";
        } else if (exception instanceof DataIntegrityViolationException) {
            status = 409; code = "FILE_IN_USE"; message = "다른 데이터에서 사용 중인 파일은 삭제할 수 없습니다.";
        } else if (exception instanceof NoSuchFileException
                || (exception instanceof S3Exception && ((S3Exception) exception).statusCode() == 404)) {
            status = 404; code = "FILE_NOT_FOUND"; message = "파일을 찾을 수 없습니다.";
        } else if (exception instanceof IOException || exception instanceof SdkException) {
            status = 503; code = "FILE_STORAGE_UNAVAILABLE"; message = "파일 저장소를 사용할 수 없습니다.";
        } else if (exception instanceof org.springframework.web.bind.MissingServletRequestParameterException
                || exception instanceof org.springframework.web.multipart.support.MissingServletRequestPartException
                || exception instanceof org.springframework.web.method.annotation.MethodArgumentTypeMismatchException) {
            status = 400; code = "FILE_REQUEST_INVALID"; message = "요청값을 확인해주세요.";
        }
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
        public FileException(int status, String code, String message, String field) {
            super(message); this.status = status; this.code = code; this.field = field;
        }
    }
}
