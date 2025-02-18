package com.doldev.dollog.domain.account.user.dto.req;

import lombok.Getter;

@Getter
public class UserUpdateReqDto {
        private String email;
        private String nickname;
        private String username;
        private String currentPassword;
        private String newPassword;
}
