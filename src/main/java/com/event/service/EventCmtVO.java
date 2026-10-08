package com.event.service;

import java.io.Serializable;
import java.util.Date;

public class EventCmtVO implements Serializable {

    private static final long serialVersionUID = 1L;

    // 필드 선언부
    private Long cmtId;      // 댓글 ID
    private Long eid;        // 이벤트 ID
    private Long uid;        // 사용자 ID
    private String nickname; // 작성자 닉네임
    private String cmt;      // 댓글 내용
    private Long parentCmtId; // 부모 댓글 ID
    private String status;   // 상태
    private Date cDate;      // 생성일
    private Date uDate;      // 수정일

    // Getter / Setter
    public Long getCmtId() {
        return cmtId;
    }

    public void setCmtId(Long cmtId) {
        this.cmtId = cmtId;
    }

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

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getCmt() {
        return cmt;
    }

    public void setCmt(String cmt) {
        this.cmt = cmt;
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