package com.event.service;

import com.cmm.service.ComFilterVO;

public class EventFilterVO extends ComFilterVO {

    private static final long serialVersionUID = 1L;

    private String title; // 제목
    private Long createdBy; // 등록자 ID
    private String ctg; // 분류
    private Long reg_id; // 지역 ID
    private Long parentRegId; // 상위 지역 ID

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public String getCtg() {
        return ctg;
    }

    public void setCtg(String ctg) {
        this.ctg = ctg;
    }

    public Long getReg_id() {
        return reg_id;
    }

    public void setReg_id(Long reg_id) {
        this.reg_id = reg_id;
    }

    public Long getParentRegId() {
        return parentRegId;
    }

    public void setParentRegId(Long parentRegId) {
        this.parentRegId = parentRegId;
    }
}
