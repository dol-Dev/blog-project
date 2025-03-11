package com.doldev.dollog.domain.account.snsUser.application;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.doldev.dollog.domain.account.snsUser.entity.SnsUser;
import com.doldev.dollog.domain.account.snsUser.enums.SnsProvider;
import com.doldev.dollog.domain.account.snsUser.enums.SnsType;
import com.doldev.dollog.domain.account.snsUser.repository.SnsUserRepository;
import com.doldev.dollog.domain.account.snsUser.service.SnsUserRegistrationService;
import com.doldev.dollog.domain.account.snsUser.util.SnsOAuth2Utils;
import com.doldev.dollog.global.auth.service.TokenCookieService;
import com.doldev.dollog.global.auth.service.TokenService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnsService {
    private final SnsOAuth2Utils snsUtils;
    private final SnsUserRepository snsUserRepository;
    private final SnsUserRegistrationService snsUserRegistrationService;
    private final TokenService tokenService;
    private final TokenCookieService tokenCookieService;

    @Value("${kakao.client.id}")
    private String kakaoClientId;
    @Value("${kakao.key}")
    private String kakaoKey;
    @Value("${naver.client.id}")
    private String naverClientId;
    @Value("${naver.client.secret}")
    private String naverClientSecret;

    public void process(SnsType snsType, String code, HttpServletRequest req, HttpServletResponse res) {
        try {
            String accessToken = getAccessToken(snsType, code);

            String username = snsUtils.gerUsername(accessToken, snsType);

            signupOrLogin(username, snsType, req, res);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getAccessToken(SnsType snsType, String code) {
        return switch (snsType) {
            case KAKAO -> snsUtils.getAccessToken(
                    code, snsType,
                    kakaoClientId, null,
                    "http://localhost:8080/api/oauth2/kakao/callback");
            case NAVER -> snsUtils.getAccessToken(
                    code, snsType,
                    naverClientId, naverClientSecret,
                    null);
        };
    }

    private void signupOrLogin(String username, SnsType snsType, HttpServletRequest req, HttpServletResponse res) {

        SnsProvider provider = SnsProvider.valueOf(snsType.getProvider());

        // 기존 사용자 조회 및 신규 사용자 등록
        SnsUser snsUser = snsUserRepository.findByUsernameAndProvider(username, provider)
                .orElseGet(() -> {
                    // 신규 사용자 등록 후 반환
                    return snsUserRegistrationService.registerNewUser(username, provider);
                });

        // 인증 처리
        setAuthenticationSnsUser(snsUser);
        Map<String, String> tokens = tokenService.generateNewTokens(username);
        tokenCookieService.setTokens(res, tokens);
    }

    public void setAuthenticationSnsUser(SnsUser snsUser) {
        Collection<? extends GrantedAuthority> authorities = Collections
                .singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        Authentication auth = new UsernamePasswordAuthenticationToken(
                snsUser.getUsername(),
                null,
                authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}