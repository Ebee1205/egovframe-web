package com.event.service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import com.cmm.service.file.FileVO;

public class EventVO implements Serializable {

    private static final long serialVersionUID = 1L;

    // 필드 선언부
    private Long eid; // 이벤트 ID
    private String title; // 제목
    private String ctg; // 분류
    private String status; // 상태
    private Long reg_id; // 지역 ID
    private Date sDate; // 시작일
    private Date eDate; // 종료일
    private String dsc; // 내용
    private String address; // 주소
    private BigDecimal lat; // 위도
    private BigDecimal lon; // 경도
    private List<Long> fileIds = new ArrayList<Long>(); // 첨부 파일 ID 목록
    private List<FileVO> attachedFiles = new ArrayList<FileVO>();
    private Long createdBy; // 등록자 ID
    private String createdByNickname; // 등록자 닉네임
    private Date cDate; // 생성일
    private Date uDate; // 수정일

    // Getter / Setter
    public Long getEid() {
        return eid;
    }

    public void setEid(Long eid) {
        this.eid = eid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCtg() {
        return ctg;
    }

    public void setCtg(String ctg) {
        this.ctg = ctg;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getReg_id() {
        return reg_id;
    }

    public void setReg_id(Long reg_id) {
        this.reg_id = reg_id;
    }

    public Date getSDate() {
        return sDate;
    }

    public void setSDate(Date sDate) {
        this.sDate = sDate;
    }

    public Date getEDate() {
        return eDate;
    }

    public void setEDate(Date eDate) {
        this.eDate = eDate;
    }

    public String getDsc() {
        return dsc;
    }

    public void setDsc(String dsc) {
        this.dsc = dsc;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigDecimal getLat() {
        return lat;
    }

    public void setLat(BigDecimal lat) {
        this.lat = lat;
    }

    public BigDecimal getLon() {
        return lon;
    }

    public void setLon(BigDecimal lon) {
        this.lon = lon;
    }

    public List<Long> getFileIds() {
        return fileIds;
    }

    public void setFileIds(List<Long> fileIds) {
        this.fileIds = fileIds == null ? new ArrayList<Long>() : fileIds;
    }

    public List<FileVO> getAttachedFiles() {
        return attachedFiles;
    }

    public void setAttachedFiles(List<FileVO> attachedFiles) {
        this.attachedFiles = attachedFiles == null ? new ArrayList<FileVO>() : attachedFiles;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedByNickname() {
        return createdByNickname;
    }

    public void setCreatedByNickname(String createdByNickname) {
        this.createdByNickname = createdByNickname;
    }

    public Date getCDate() {
        return cDate;
    }

    public void setCDate(Date cDate) {
        this.cDate = cDate;
    }

    public Date getUDate() {
        return uDate;
    }

    public void setUDate(Date uDate) {
        this.uDate = uDate;
    }
}