package com.event.service;

import com.cmm.ComFilterVO;

public class EventCmtFilterVO extends ComFilterVO {

    private static final long serialVersionUID = 1L;

    private Long eid;          // 이벤트 ID
    private Long uid;          // 사용자 ID
    private Long parentCmtId;  // 부모 댓글 ID
    private String status;     // 상태

    public Long getEid() {
        return eid;
    }

    public void setEid(Long eid) {
        this.eid = eid;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public Long getParentCmtId() {
        return parentCmtId;
    }

    public void setParentCmtId(Long parentCmtId) {
        this.parentCmtId = parentCmtId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}