package com.doldev.dollog.global.auth.dto.res;

import com.doldev.dollog.global.auth.principal.CustomUserDetails;

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
        private String avatarImageName;
        private String blogName;

        public static AuthenticatedUserResDto from(CustomUserDetails userDetails) {
                String avatarImageName = null;
                String blogName = null;

                if (userDetails.getUser() != null) {
                        avatarImageName = userDetails.getUser().getProfile().getAvatarImageName();
                        blogName = userDetails.getUser().getProfile().getBlogName();
                } else if (userDetails.getSnsUser() != null) {
                        avatarImageName = userDetails.getSnsUser().getProfile().getAvatarImageName();
                        blogName = userDetails.getSnsUser().getProfile().getBlogName();
                }

                return AuthenticatedUserResDto.builder()
                                .id(userDetails.getId())
                                .username(userDetails.getUsername())
                                .nickname(userDetails.getNickname())
                                .provider(userDetails.getProvider())
                                .email(userDetails.getEmail())
                                .avatarImageName(avatarImageName)
                                .blogName(blogName)
                                .build();
        }
}
