package com.doldev.dollog.domain.account.user.dto.req;

import lombok.Getter;

@Getter
public class PasswordUpdateReqDto {
    private String username;
    private String currentPassword;
    private String newPassword;
}