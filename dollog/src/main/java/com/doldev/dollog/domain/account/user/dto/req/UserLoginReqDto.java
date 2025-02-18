package com.doldev.dollog.domain.account.user.dto.req;

import lombok.Getter;

@Getter
public class UserLoginReqDto {
    private String username;
    private String password;
}