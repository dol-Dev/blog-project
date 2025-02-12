package com.doldev.dollog.domain.account.user.dto.req;

import lombok.Data;

@Data
public class UpdateUserReqDto {
        private String email;
        private String password;
        private String nickname;
        private String username;
        private String currentPassword;
        private String newPassword;
}
