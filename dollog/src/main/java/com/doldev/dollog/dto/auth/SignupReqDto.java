package com.doldev.dollog.dto.auth;

import lombok.Data;

@Data
public class SignupReqDto {
    
    private String username;

    private String password;

    private String email;

    private String nickname;
}
