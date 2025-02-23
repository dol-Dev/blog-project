package com.doldev.dollog.domain.account.profile.dto.req;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NicknameUpdateReqDto {
    private String username;
    private String nickname;
}