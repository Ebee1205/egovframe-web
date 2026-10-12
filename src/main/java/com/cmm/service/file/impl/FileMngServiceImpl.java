package com.cmm.service.file.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import com.cmm.service.file.FileMngService;
import com.cmm.service.file.FileVO;
import com.cmm.storage.FileStorage;

@Service("fileMngService")
@Transactional(rollbackFor = Exception.class)
public class FileMngServiceImpl implements FileMngService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileMngServiceImpl.class);
    private final FileStorage storage;
    private final FileDAO dao;
    private final FileMngUtil util;

    public FileMngServiceImpl(FileStorage storage, FileDAO dao, FileMngUtil util) {
        this.storage = storage; this.dao = dao; this.util = util;
    }
    @Override public void checkStorageConnection() { storage.checkConnection(); }

    private void location(FileVO file) {
        if (!storage.getLocation().equals(file.getFileStreCours())) {
            throw new FileMngUtil.FileException(409, "FILE_STORAGE_MISMATCH", "파일이 저장된 저장소 설정을 확인해주세요.", null);
        }
    }
    private FileVO find(Long id, boolean lock) {
        FileVO file = lock ? dao.selectFileForUpdate(FileMngUtil.id(id)) : dao.selectFileInf(FileMngUtil.id(id));
        if (file == null) { throw new FileMngUtil.FileException(404, "FILE_NOT_FOUND", "파일을 찾을 수 없습니다.", "fileId"); }
        return file;
    }
    private void cleanup(String key) {
        try { storage.delete(key); }
        catch (Exception exception) { LOGGER.error("Stored object cleanup failed; retry required for key {}", key, exception); }
    }
    private void rollbackCleanup(String key) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) { cleanup(key); }
                else if (status == STATUS_UNKNOWN) { LOGGER.error("Transaction outcome unknown; inspect storage key {}", key); }
            }
        });
    }
    private void commitCleanup(String key) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() { cleanup(key); }
        });
    }
    private void write(MultipartFile upload, FileVO file) throws IOException {
        // Register before put: a provider can fail after having persisted the object.
        rollbackCleanup(file.getStreFileNm());
        try (InputStream input = upload.getInputStream()) {
            storage.put(file.getStreFileNm(), input, file.getFileSize(), FileMngUtil.contentType(file.getFileExtsn()));
        }
    }
    @Override public List<FileVO> uploadFiles(List<MultipartFile> uploads, String description) throws IOException {
        util.validateCount(uploads == null ? 0 : uploads.size());
        List<FileVO> files = new ArrayList<>();
        // Validate the whole request before writing any object.
        for (MultipartFile upload : uploads) { files.add(util.describe(upload, storage.getLocation(), description)); }
        for (int i = 0; i < files.size(); i++) { write(uploads.get(i), files.get(i)); dao.insertFileInf(files.get(i)); }
        return files;
    }
    @Override public FileVO updateFile(Long fileId, MultipartFile replacement, String description) throws IOException {
        FileVO current = find(fileId, true);
        location(current);
        if (replacement == null) {
            if (description != null) { current.setFileCn(description); }
            dao.updateFileInf(current);
            return current;
        }
        FileVO next = util.describe(replacement, storage.getLocation(), description == null ? current.getFileCn() : description);
        next.setFileId(current.getFileId());
        next.setCDate(current.getCDate());
        write(replacement, next);
        dao.updateFileInf(next);
        commitCleanup(current.getStreFileNm());
        return next;
    }
    @Override @Transactional(readOnly = true)
    public InputStream openFile(FileVO file) throws IOException {
        // Re-read trusted metadata rather than trusting a supplied path/key.
        FileVO stored = find(file.getFileId(), false);
        location(stored);
        if (!stored.getStreFileNm().equals(file.getStreFileNm())) {
            throw new FileMngUtil.FileException(409, "FILE_CHANGED", "파일이 변경되었습니다. 다시 다운로드해주세요.", null);
        }
        return storage.open(stored.getStreFileNm());
    }
    @Override @Transactional(readOnly = true)
    public List<FileVO> selectFileInfs(FileVO filter) {
        return dao.selectFileInfs(filter == null ? new FileVO() : filter);
    }
    @Override @Transactional(readOnly = true)
    public FileVO selectFileInf(FileVO file) { return find(file == null ? null : file.getFileId(), false); }
    private void validateStored(FileVO file) throws IOException {
        if (file == null || file.getFileId() != null || file.getFileSize() == null || file.getFileSize() <= 0
                || file.getOrignlFileNm() == null || file.getOrignlFileNm().length() > 255
                || file.getFileExtsn() == null || file.getFileExtsn().length() > 20
                || file.getStreFileNm() == null || file.getStreFileNm().length() > 255) {
            throw FileMngUtil.bad("FILE_METADATA_INVALID", "파일 정보를 확인해주세요.", "file");
        }
        location(file);
        try (InputStream input = storage.open(file.getStreFileNm())) { /* Verify the saved object exists. */ }
    }
    @Override public String insertFileInf(FileVO file) throws IOException {
        validateStored(file); dao.insertFileInf(file); return file.getFileId().toString();
    }
    @Override public String insertFileInfs(List<FileVO> files) throws IOException {
        util.validateCount(files == null ? 0 : files.size());
        for (FileVO file : files) { validateStored(file); }
        StringJoiner ids = new StringJoiner(",");
        for (FileVO file : files) { dao.insertFileInf(file); ids.add(file.getFileId().toString()); }
        return ids.toString();
    }
    @Override public void updateFileInfs(List<FileVO> files) {
        if (files == null || files.isEmpty()) { throw FileMngUtil.bad("FILE_LIST_EMPTY", "파일 목록을 확인해주세요.", "files"); }
        List<FileVO> sorted = new ArrayList<>(files);
        for (FileVO file : sorted) { FileMngUtil.id(file == null ? null : file.getFileId()); }
        sorted.sort(Comparator.comparing(FileVO::getFileId));
        for (FileVO change : sorted) {
            FileVO stored = find(change.getFileId(), true);
            stored.setFileCn(change.getFileCn()); dao.updateFileInf(stored);
        }
    }
    @Override public void deleteFileInf(FileVO file) {
        FileVO stored = find(file == null ? null : file.getFileId(), true);
        location(stored);
        // FK rejection or DB rollback leaves the stored object untouched.
        dao.deleteFileInf(stored.getFileId()); commitCleanup(stored.getStreFileNm());
    }
    @Override public void deleteFileInfs(List<FileVO> files) {
        if (files == null || files.isEmpty()) { throw FileMngUtil.bad("FILE_LIST_EMPTY", "파일 목록을 확인해주세요.", "files"); }
        List<FileVO> sorted = new ArrayList<>(files);
        for (FileVO file : sorted) { FileMngUtil.id(file == null ? null : file.getFileId()); }
        sorted.sort(Comparator.comparing(FileVO::getFileId));
        java.util.Set<Long> ids = new java.util.HashSet<>();
        for (FileVO file : sorted) { if (ids.add(file.getFileId())) { deleteFileInf(file); } }
    }
    @Override @Transactional(readOnly = true)
    public Map<String, Object> selectFileListByFileNm(FileVO filter) {
        List<FileVO> files = selectFileInfs(filter);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("items", files); return result;
    }
    @Override @Transactional(readOnly = true)
    public List<FileVO> selectImageFileList(FileVO filter) {
        if (filter == null) { filter = new FileVO(); }
        boolean previous = filter.isImagesOnly();
        try { filter.setImagesOnly(true); return selectFileInfs(filter); }
        finally { filter.setImagesOnly(previous); }
    }
}
