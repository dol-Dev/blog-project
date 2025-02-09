package com.doldev.dollog.global.auth.filter;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import com.doldev.dollog.global.auth.service.CustomUserDetailsService;
import com.doldev.dollog.global.auth.service.JwtTokenProvider;
import com.doldev.dollog.global.auth.service.TokenManager;
import com.doldev.dollog.global.dto.ApiResDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtProvider;
    private final TokenManager tokenManager;
    private final CustomUserDetailsService userDetailsService;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.env}")
    private String env;

    @SuppressWarnings("null")
    // 요청마다 쿠키에서 액세스 토큰 추출, 유효하면 인증 설정, 아니면 리프레시 토큰 기반 토큰 갱신 시도
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain filterChain)
            throws ServletException, IOException {
        log.info("Starting authentication filter...");

        try {
            String accessToken = extractAccessToken(req);

            if (accessToken != null && jwtProvider.validateToken(accessToken)) {
                // 유효한 액세스 토큰이면 바로 인증 설정
                String username = jwtProvider.getUserInfoFromToken(accessToken).getSubject();
                setAuthentication(username);
            } else {
                // 액세스 토큰 없거나 만료된 경우, 리프레시 토큰으로 토큰 갱신 시도
                handleTokenRefresh(req, res);
            }

            filterChain.doFilter(req, res);
        } catch (Exception e) {
            log.error("Authentication error: {}", e.getMessage());
            sendErrorResponse(res, "TOKEN_EXPIRED", "Session expired. Please re-login.");
        }

        log.info("Authentication filter processing complete.");
    }

    // 리프레시 토큰을 기반으로 토큰 갱신 및 인증 설정 시도
    private void handleTokenRefresh(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String refreshToken = extractRefreshToken(req);

        if (refreshToken != null && jwtProvider.validateToken(refreshToken)) {
            String username = jwtProvider.getUserInfoFromToken(refreshToken).getSubject();
            String storedRefreshToken = redisTemplate.opsForValue().get("refresh_" + username);

            if (refreshToken.equals(storedRefreshToken)) {
                // 리프레시 토큰 일치하면 새 토큰 발급 후 쿠키와 인증 갱신
                Map<String, String> newTokens = tokenManager.generateNewTokens(username);
                setTokenCookies(res, newTokens);
                setAuthentication(username);
                log.info("Token refresh successful for user: {}", username);
                return;
            }
        }
        log.warn("Invalid refresh token attempt");
        clearInvalidCookies(res);
    }

    // 요청의 쿠키에서 URL 디코딩된 액세스 토큰 추출
    private String extractAccessToken(HttpServletRequest req) {
        if (req.getCookies() != null) {
            return Arrays.stream(req.getCookies())
                    .filter(cookie -> "accessToken".equals(cookie.getName()))
                    .findFirst()
                    .map(cookie -> URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8))
                    .orElse(null);
        }
        return null;
    }

    // 요청의 쿠키에서 URL 디코딩된 리프레시 토큰 추출
    private String extractRefreshToken(HttpServletRequest req) {
        if (req.getCookies() != null) {
            return Arrays.stream(req.getCookies())
                    .filter(cookie -> "refreshToken".equals(cookie.getName()))
                    .findFirst()
                    .map(cookie -> URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8))
                    .orElse(null);
        }
        return null;
    }

    // 새 토큰들을 HttpOnly 쿠키에 설정
    private void setTokenCookies(HttpServletResponse res, Map<String, String> tokens) {
        ResponseCookie accessCookie = buildCookie("accessToken", tokens.get("accessToken"), 30 * 60);
        ResponseCookie refreshCookie = buildCookie("refreshToken", tokens.get("refreshToken"), 14 * 24 * 60 * 60);

        res.addHeader("Set-Cookie", accessCookie.toString());
        res.addHeader("Set-Cookie", refreshCookie.toString());
    }

    // 이름, 값, 만료 시간을 받아 URL 인코딩, HttpOnly, SameSite, Secure 옵션이 적용된 쿠키 생성
    private ResponseCookie buildCookie(String name, String value, int maxAge) {
        return ResponseCookie.from(name, URLEncoder.encode(value, StandardCharsets.UTF_8))
                .path("/")
                .maxAge(Duration.ofSeconds(maxAge))
                .httpOnly(true)
                .sameSite("Strict")
                .secure("prod".equals(env))
                .build();
    }

    // 무효한 토큰이면 쿠키 삭제(만료) 처리
    private void clearInvalidCookies(HttpServletResponse res) {
        ResponseCookie clearAccess = buildCookie("accessToken", "", 0);
        ResponseCookie clearRefresh = buildCookie("refreshToken", "", 0);
        res.addHeader("Set-Cookie", clearAccess.toString());
        res.addHeader("Set-Cookie", clearRefresh.toString());
    }

    // 에러 응답을 JSON 형태로 전송
    private void sendErrorResponse(HttpServletResponse res, String code, String message) throws IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        String json = objectMapper.writeValueAsString(
                ApiResDto.builder()
                        .messageCode(code)
                        .build());
        res.getWriter().write(json);
    }

    // username 기반으로 UserDetails 조회 후 SecurityContext에 인증 객체 설정
    private void setAuthentication(String username) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}