package com.event.service;

import com.cmm.ComFilterVO;

public class EventFilterVO extends ComFilterVO {

    private static final long serialVersionUID = 1L;

    private String title; // 제목
    private Long createdBy; // 등록자 ID
    private String ctg; // 분류

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
}