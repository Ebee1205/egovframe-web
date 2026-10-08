package com.cmm.file;

import java.io.Serializable;
import java.util.Date;

public class FileVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long fileId;
    private String fileStreCours;
    private String streFileNm;
    private String orignlFileNm;
    private String fileExtsn;
    private String fileCn;
    private Long fileSize;
    private Date cDate;

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public String getFileStreCours() {
        return fileStreCours;
    }

    public void setFileStreCours(String fileStreCours) {
        this.fileStreCours = fileStreCours;
    }

    public String getStreFileNm() {
        return streFileNm;
    }

    public void setStreFileNm(String streFileNm) {
        this.streFileNm = streFileNm;
    }

    public String getOrignlFileNm() {
        return orignlFileNm;
    }

    public void setOrignlFileNm(String orignlFileNm) {
        this.orignlFileNm = orignlFileNm;
    }

    public String getFileExtsn() {
        return fileExtsn;
    }

    public void setFileExtsn(String fileExtsn) {
        this.fileExtsn = fileExtsn;
    }

    public String getFileCn() {
        return fileCn;
    }

    public void setFileCn(String fileCn) {
        this.fileCn = fileCn;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Date getCDate() {
        return cDate;
    }

    public void setCDate(Date cDate) {
        this.cDate = cDate;
    }
}