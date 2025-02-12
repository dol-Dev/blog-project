package com.doldev.dollog.domain.account.user.dto.req;

import lombok.Getter;

@Getter
public class LoginReqDto {
    private String username;
    private String password;
}