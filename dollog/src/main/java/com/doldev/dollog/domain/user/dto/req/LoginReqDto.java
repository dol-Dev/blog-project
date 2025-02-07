package com.doldev.dollog.domain.user.dto.req;

import lombok.Getter;

@Getter
public class LoginReqDto {
    private String username;
    private String password;
}