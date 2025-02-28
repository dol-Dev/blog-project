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
        return AuthenticatedUserResDto.builder()
                .id(userDetails.getId())
                .username(userDetails.getUsername())
                .nickname(userDetails.getNickname())
                .provider(userDetails.getProvider())
                .email(userDetails.getEmail())
                .avatarImageName(
                        userDetails.getUser() != null
                                ? userDetails.getUser().getProfile().getAvatarImageName()
                                : userDetails.getSnsUser() != null
                                        ? userDetails.getSnsUser().getProfile().getAvatarImageName()
                                        : null)
                .blogName(
                        userDetails.getUser() != null
                                ? userDetails.getUser().getProfile().getBlogName()
                                : userDetails.getSnsUser() != null
                                        ? userDetails.getSnsUser().getProfile().getBlogName()
                                        : null)
                .build();
    }
}
