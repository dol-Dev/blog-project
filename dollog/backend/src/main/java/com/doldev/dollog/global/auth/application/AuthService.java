package com.doldev.dollog.global.auth.application;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.doldev.dollog.domain.account.user.dto.req.UserLoginReqDto;
import com.doldev.dollog.global.auth.dto.res.AuthenticatedUserResDto;
import com.doldev.dollog.global.auth.principal.CustomUserDetails;
import com.doldev.dollog.global.auth.service.CookieManager;
import com.doldev.dollog.global.auth.service.TokenService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {

    private final RedisTemplate<String, String> redisTemplate;
    private final TokenService tokenService;
    private final CookieManager cookieManager;
    private final AuthenticationManager authenticationManager;

    // 로그인
    public void login(UserLoginReqDto reqDto, HttpServletResponse res) {
        setBeforeLoginAuthenticationUser(reqDto);
        Map<String, String> tokens = tokenService.generateNewTokens(reqDto.getUsername());
        cookieManager.setTokens(res, tokens);
    }

    // 로그아웃
    public void logout(CustomUserDetails userDetails, HttpServletRequest req, HttpServletResponse res) {
        String refreshKey = "refresh_" + userDetails.getUsername();
        String storedRefreshToken = Optional.ofNullable(redisTemplate.opsForValue().get(refreshKey))
                .map(token -> token.substring(7))
                .orElseThrow(() -> new IllegalArgumentException("Refresh token not found in Redis."));

        Optional<String> refreshTokenOpt = cookieManager.extractRefreshToken(req);

        refreshTokenOpt.ifPresent(r -> {
            if (!StringUtils.equals(r, storedRefreshToken)) {
                throw new IllegalArgumentException("Invalid refresh token.");
            }
        });

        redisTemplate.delete(refreshKey);
        cookieManager.clearTokens(res);
    }

    // 인증된 사용자 정보 반환
    public AuthenticatedUserResDto getUserInfo(CustomUserDetails userDetails) {
        return AuthenticatedUserResDto.from(userDetails);
    }

    // 일반사용자 로그인 요청 시 인증객체 설정
    private void setBeforeLoginAuthenticationUser(UserLoginReqDto reqDto) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        reqDto.getUsername(),
                        reqDto.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
