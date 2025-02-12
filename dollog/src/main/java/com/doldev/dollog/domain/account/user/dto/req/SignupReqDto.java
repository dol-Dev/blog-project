package com.doldev.dollog.domain.account.user.dto.req;

import lombok.Data;

@Data
public class SignupReqDto {
    private String username;
    private String password;
    private String email;
    private String nickname;
}
