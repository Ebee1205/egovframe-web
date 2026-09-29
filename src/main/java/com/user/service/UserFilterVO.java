package com.user.service;

import com.cmm.ComFilterVO;

public class UserFilterVO extends ComFilterVO {

    private static final long serialVersionUID = 1L;

    private String email;
    private String nickname;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}