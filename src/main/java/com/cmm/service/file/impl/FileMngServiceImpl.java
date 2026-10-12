package com.cmm.service.file.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.cmm.service.file.FileMngService;
import com.cmm.service.file.FileVO;
import com.cmm.storage.FileStorage;

@Service("fileMngService")
public class FileMngServiceImpl implements FileMngService {

    private final FileStorage fileStorage;

    public FileMngServiceImpl(FileStorage fileStorage) {
        this.fileStorage = fileStorage;
    }

    @Override
    public void checkStorageConnection() {
        fileStorage.checkConnection();
    }

    @Override
    public List<FileVO> selectFileInfs(FileVO fvo) {
        throw unsupported("selectFileInfs");
    }

    @Override
    public String insertFileInf(FileVO fvo) {
        throw unsupported("insertFileInf");
    }

    @Override
    public String insertFileInfs(List<FileVO> fvoList) {
        throw unsupported("insertFileInfs");
    }

    @Override
    public void updateFileInfs(List<FileVO> fvoList) {
        throw unsupported("updateFileInfs");
    }

    @Override
    public void deleteFileInfs(List<FileVO> fvoList) {
        throw unsupported("deleteFileInfs");
    }

    @Override
    public void deleteFileInf(FileVO fvo) {
        throw unsupported("deleteFileInf");
    }

    @Override
    public FileVO selectFileInf(FileVO fvo) {
        throw unsupported("selectFileInf");
    }

    @Override
    public int getMaxFileSN(FileVO fvo) {
        throw unsupported("getMaxFileSN");
    }

    @Override
    public void deleteAllFileInf(FileVO fvo) {
        throw unsupported("deleteAllFileInf");
    }

    @Override
    public Map<String, Object> selectFileListByFileNm(FileVO fvo) {
        throw unsupported("selectFileListByFileNm");
    }

    @Override
    public List<FileVO> selectImageFileList(FileVO fvo) {
        throw unsupported("selectImageFileList");
    }

    private UnsupportedOperationException unsupported(String operation) {
        return new UnsupportedOperationException(operation + " is not supported until file persistence is implemented.");
    }
}