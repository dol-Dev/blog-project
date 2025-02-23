package com.doldev.dollog.global.auth.service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TokenService {
    private final JwtTokenProvider jwtProvider;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.refresh.expiration}")
    private long refreshExpirationTime;

    // 로그인 시 토큰 생성
    public Map<String, String> generateTokens(String username) {
        String refreshTokenKey = "refresh_" + username;

        String accessToken = jwtProvider.createAccessToken(username);
        String refreshToken = jwtProvider.createRefreshToken(username);

        redisTemplate.opsForValue().set(
                refreshTokenKey,
                refreshToken,
                refreshExpirationTime,
                TimeUnit.MILLISECONDS);

        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken);
    }

    // 기존 리프레시 토큰을 기반한 새 토큰들 생성
    public Map<String, String> generateNewTokens(String username) {
        String refreshTokenKey = "refresh_" + username;

        String existingRefreshToken = redisTemplate.opsForValue().get(refreshTokenKey);
        if (existingRefreshToken == null) {
            throw new IllegalStateException("유효한 리프레시 토큰이 없습니다.");
        }

        redisTemplate.delete(refreshTokenKey);

        String newAccessToken = jwtProvider.createAccessToken(username);
        String newRefreshToken = jwtProvider.createRefreshToken(username);

        redisTemplate.opsForValue().set(
                refreshTokenKey,
                newRefreshToken,
                refreshExpirationTime,
                TimeUnit.MILLISECONDS);

        return Map.of(
                "accessToken", newAccessToken,
                "refreshToken", newRefreshToken);
    }
}
