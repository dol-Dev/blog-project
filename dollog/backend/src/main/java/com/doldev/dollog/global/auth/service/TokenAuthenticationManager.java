package com.doldev.dollog.global.auth.service;

import java.util.Map;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.doldev.dollog.domain.account.snsUser.entity.SnsUser;
import com.doldev.dollog.domain.account.user.dto.req.UserLoginReqDto;
import com.doldev.dollog.global.auth.principal.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenAuthenticationManager {

    private final JwtTokenProvider jwtProvider;
    private final TokenService tokenService;
    private final CustomUserDetailsService userDetailsService;
    private final RedisTemplate<String, String> redisTemplate;
    private final AuthenticationManager authenticationManager;

    // 일반사용자 로그인 요청 시 인증객체 설정
    public void setBeforeLoginAuthenticationUser(UserLoginReqDto reqDto) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        reqDto.getUsername(),
                        reqDto.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // sns사용자 로그인 요청 시 인증객체 설정
    public void setAuthenticationSnsUser(SnsUser snsUser) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        snsUser.getUsername(),
                        null));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // 인증필터 컨텍스트 설정
    public void setAuthenticationForFilter(String token) {
        String username = extractUsername(token); // 토큰에서 username 추출
        CustomUserDetails userDetails;

        try {
            // 일반 로그인 사용자 조회
            userDetails = userDetailsService.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            // 일반 사용자가 없으면 SNS 사용자 조회
            try {
                userDetails = userDetailsService.loadSnsUserByUsername(username);
            } catch (UsernameNotFoundException ex) {
                throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
            }
        }

        // 인증 객체 생성 및 컨텍스트에 설정
        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // 토큰 유효성 검증
    public boolean isValidToken(String token) {
        return jwtProvider.validateToken(token);
    }

    // 리프레시 토큰으로 새 토큰 발급
    public Map<String, String> refreshTokens(String refreshToken) {
        validateRefreshToken(refreshToken);
        String username = extractUsername(refreshToken);
        return tokenService.generateNewTokens(username);
    }

    // 내부: 리프레시 토큰 검증
    private void validateRefreshToken(String token) {
        if (!isValidToken(token)) {
            throw new InvalidTokenException("유효하지 않은 리프레시 토큰");
        }

        String username = extractUsername(token);
        String storedToken = redisTemplate.opsForValue().get("refresh_" + username);
        if (!token.equals(storedToken)) {
            throw new TokenMismatchException("저장된 토큰과 불일치");
        }
    }

    // 내부: 토큰에서 사용자명 추출
    private String extractUsername(String token) {
        return jwtProvider.getUserInfoFromToken(token).getSubject();
    }

    // === 커스텀 예외 ===
    public static class InvalidTokenException extends RuntimeException {
        public InvalidTokenException(String message) {
            super(message);
        }
    }

    public static class TokenMismatchException extends RuntimeException {
        public TokenMismatchException(String message) {
            super(message);
        }
    }
}
