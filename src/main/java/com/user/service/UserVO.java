package com.user.service;

import java.io.Serializable;
import java.util.Date;

public class UserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long uid;
    private String email;
    private String pwd;
    private String nickname;
    private String type;
    private String status;
    private Long rid;
    private Date cDate;
    private Date uDate;

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getRid() {
        return rid;
    }

    public void setRid(Long rid) {
        this.rid = rid;
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