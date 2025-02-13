package com.doldev.dollog.global.auth.dto.res;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthenticatedUserResDto {
    private int id;
    private String username;
    private String nickname;
    private String provider;
    private String email;
}
