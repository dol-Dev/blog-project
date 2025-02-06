package com.doldev.dollog.dto.auth;

import lombok.Getter;

@Getter
public class LoginReqDto {
    private String username;
    private String password;
}