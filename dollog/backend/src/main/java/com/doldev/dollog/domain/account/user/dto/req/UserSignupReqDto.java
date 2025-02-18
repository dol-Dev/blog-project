package com.doldev.dollog.domain.account.user.dto.req;

import lombok.Getter;

@Getter
public class UserSignupReqDto {
    private String username;
    private String password;
    private String email;
    private String nickname;
}
