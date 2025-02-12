package com.doldev.dollog.global.auth.application;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.doldev.dollog.global.auth.principal.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final RedisTemplate<String, String> redisTemplate;

    // 로그아웃
    public void logout(CustomUserDetails userDetails, String accessToken, String refreshToken) {
        String username = userDetails.getUsername();
        String refreshKey = "refresh_" + username;
        String storedRefreshToken = redisTemplate.opsForValue().get(refreshKey);

        // 전달된 refreshToken과 Redis에 저장된 값 비교
        if (!StringUtils.equals(refreshKey, storedRefreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token.");
        }

        redisTemplate.delete(refreshKey);
    }
}
