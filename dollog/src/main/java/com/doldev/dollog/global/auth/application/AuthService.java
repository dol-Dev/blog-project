package com.doldev.dollog.global.auth.application;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.doldev.dollog.domain.account.user.dto.req.LoginReqDto;
import com.doldev.dollog.global.auth.principal.CustomUserDetails;
import com.doldev.dollog.global.auth.service.CookieManager;
import com.doldev.dollog.global.auth.service.TokenAuthenticationManager;
import com.doldev.dollog.global.auth.service.TokenService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final RedisTemplate<String, String> redisTemplate;
    private final TokenService tokenService;
    private final TokenAuthenticationManager tokenAuthenticationManager;
    private final CookieManager cookieManager;

    // 로그인
    public void login(LoginReqDto reqDto, HttpServletResponse res) {
        tokenAuthenticationManager.setBeforeLoginAuthenticationUser(reqDto);
        Map<String, String> tokens = tokenService.generateNewTokens(reqDto.getUsername());
        cookieManager.setTokens(res, tokens);
    }

    // 로그아웃
    public void logout(CustomUserDetails userDetails, String accessToken, String refreshToken, HttpServletResponse res) {
        String username = userDetails.getUsername();
        String refreshKey = "refresh_" + username;
        String storedRefreshToken = redisTemplate.opsForValue().get(refreshKey);

        // 전달된 refreshToken과 Redis에 저장된 값 비교
        if (!StringUtils.equals(refreshKey, storedRefreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token.");
        }

        redisTemplate.delete(refreshKey);
        cookieManager.clearTokens(res);
    }
}
