package com.doldev.dollog.global.auth.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.doldev.dollog.domain.account.snsUser.entity.SnsUser;
import com.doldev.dollog.domain.account.snsUser.enums.SnsProvider;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class AuthenticationService {

    private final TokenManager tokenManager;
    private final CustomUserDetailsService userDetailsService;
    private final HttpServletResponse httpServletResponse;

    @Value("${app.env}")
    private String env;

    /**
     * 신뢰된 SNS 사용자 인증 및 토큰 쿠키 설정
     * - SNS 인증은 외부에서 이미 검증되었으므로, 비밀번호 없이 인증 객체 설정.
     */
    public void authenticateSnsUser(SnsUser snsUser) {
        // 토큰 생성 (access, refresh)
        Map<String, String> tokens = tokenManager.generateTokens(snsUser.getSnsIdentifier());
        // 쿠키에 토큰 적용 (공통 로직)
        addAuthCookies(tokens);
        // 인증 객체 적용
        setTrustedAuthentication(snsUser.getSnsIdentifier(), snsUser.getProvider());
    }

    // 쿠키에 access, refresh 토큰 적용
    private void addAuthCookies(Map<String, String> tokens) {
        ResponseCookie accessCookie = createCookie("accessToken", tokens.get("accessToken"), Duration.ofMinutes(30));
        ResponseCookie refreshCookie = createCookie("refreshToken", tokens.get("refreshToken"), Duration.ofDays(14));
        httpServletResponse.addHeader("Set-Cookie", accessCookie.toString());
        httpServletResponse.addHeader("Set-Cookie", refreshCookie.toString());
    }

    // // 쿠키에 access 토큰만 적용
    // private void addAcessCookie(String name, String token, Duration maxAge) {
    // ResponseCookie cookie = createCookie(name, token, maxAge);
    // httpServletResponse.addHeader("Set-Cookie", cookie.toString());
    // }

    // URL 인코딩 및 HttpOnly, SameSite, Secure 옵션 적용된 쿠키 생성
    private ResponseCookie createCookie(String name, String token, Duration maxAge) {
        return ResponseCookie.from(name, URLEncoder.encode(token, StandardCharsets.UTF_8))
                .path("/")
                .maxAge(maxAge)
                .httpOnly(true)
                .sameSite("Strict")
                .secure("prod".equals(env))
                .build();
    }

    // 사용자명(또는 식별자) 기반으로 인증 객체 설정 (신뢰된 SNS 사용자용)
    private void setTrustedAuthentication(String snsIdentifier, SnsProvider provider) {
        UserDetails userDetails = userDetailsService.loadSnsUserBySnsIdentifier(snsIdentifier, provider);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
