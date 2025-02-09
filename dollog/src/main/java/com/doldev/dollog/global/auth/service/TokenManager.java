package com.doldev.dollog.global.auth.service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TokenManager {
    private final JwtTokenProvider jwtProvider;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.refresh.expiration}")
    private long refreshExpirationTime;

    // 새로운 액세스 토큰과 리프레시 토큰을 생성하고, 리프레시 토큰을 Redis에 저장한 후 반환해
    public Map<String, String> generateTokens(String username) {
        String accessToken = jwtProvider.createAccessToken(username);
        String refreshToken = jwtProvider.createRefreshToken(username);

        redisTemplate.opsForValue().set(
            "refresh_" + username,
            refreshToken,
            refreshExpirationTime,
            TimeUnit.MILLISECONDS
        );

        return Map.of(
            "accessToken", accessToken,
            "refreshToken", refreshToken
        );
    }

    // 기존 리프레시 토큰을 사용하여 새로운 액세스 토큰과 리프레시 토큰을 생성하고, 기존 리프레시 토큰을 삭제한 후 새로 생성된 토큰들을 Redis에 저장하며 반환해
    public Map<String, String> generateNewTokens(String username) {
        String refreshTokenKey = "refresh_" + username;

        String existingRefreshToken = redisTemplate.opsForValue().get(refreshTokenKey);
        if (existingRefreshToken == null) {
            throw new IllegalStateException("사용 가능한 리프레시 토큰이 없어");
        }

        redisTemplate.delete(refreshTokenKey);

        String newAccessToken = jwtProvider.createAccessToken(username);
        String newRefreshToken = jwtProvider.createRefreshToken(username);

        redisTemplate.opsForValue().set(
            refreshTokenKey,
            newRefreshToken,
            refreshExpirationTime,
            TimeUnit.MILLISECONDS
        );

        return Map.of(
            "accessToken", newAccessToken,
            "refreshToken", newRefreshToken
        );
    }
}
