package com.doldev.dollog.global.auth.service;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenManager {
    private final JwtTokenProvider jwtProvider;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.token.refresh-expiration-time}")
    private long refreshExpirationTime;

    public String generateTokens(String username) {
        // 토큰 생성
        String accessToken = jwtProvider.createAccessToken(username);
        String refreshToken = jwtProvider.createRefreshToken(username);

        // Redis에 Refresh 저장
        redisTemplate.opsForValue().set("refresh_" + username, refreshToken, refreshExpirationTime,
                TimeUnit.MILLISECONDS);

        return accessToken;
    }


    public String generateNewTokens(String username) {
        String refreshTokenKey = "refresh_" + username;

        // 1. 기존 리프레시 토큰 한번 사용 후 삭제
        String existingRefreshToken = redisTemplate.opsForValue().get(refreshTokenKey);
        if (existingRefreshToken == null) {
            throw new IllegalStateException("No refresh token available");
        }
        redisTemplate.delete(refreshTokenKey);

        // 2. 새로운 토큰 쌍 생성
        String newAccessToken = jwtProvider.createAccessToken(username);
        String newRefreshToken = jwtProvider.createRefreshToken(username);

        // 3. 새 리프레시 토큰 저장
        redisTemplate.opsForValue().set(
                refreshTokenKey,
                newRefreshToken,
                refreshExpirationTime,
                TimeUnit.MILLISECONDS
                );

        return newAccessToken;
    }
}