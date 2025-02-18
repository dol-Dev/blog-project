package com.doldev.dollog.domain.account.snsUser.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SnsType {
        KAKAO(
                        "kakao",
                        "https://kauth.kakao.com/oauth/token",
                        "https://kapi.kakao.com/v2/user/me",
                        "authorization_code",
                        null),
        NAVER(
                        "naver",
                        "https://nid.naver.com/oauth2.0/token",
                        "https://openapi.naver.com/v1/nid/me",
                        "authorization_code",
                        "client_secret");

        private final String provider;
        private final String tokenUrl;
        private final String userInfoUrl;
        private final String grantType;
        private final String clientSecretKey;

}
