package com.doldev.dollog.global.auth.application;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.doldev.dollog.domain.account.snsUser.entity.SnsUser;
import com.doldev.dollog.domain.account.snsUser.repository.SnsUserRepository;
import com.doldev.dollog.domain.account.user.dto.req.UserLoginReqDto;
import com.doldev.dollog.domain.account.user.entity.User;
import com.doldev.dollog.domain.account.user.repository.UserRepository;
import com.doldev.dollog.global.auth.dto.res.AuthenticatedUserResDto;
import com.doldev.dollog.global.auth.principal.CustomUserDetails;
import com.doldev.dollog.global.auth.service.TokenCookieService;
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
    private final TokenCookieService tokenCookieService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final SnsUserRepository snsUserRepository;

    // 로그인
    public void login(UserLoginReqDto reqDto, HttpServletResponse res) {
        setBeforeLoginAuthenticationUser(reqDto);
        Map<String, String> tokens = tokenService.generateNewTokens(reqDto.getUsername());
        tokenCookieService.setTokens(res, tokens);
    }

    // 로그아웃
    public void logout(CustomUserDetails userDetails, HttpServletRequest req, HttpServletResponse res) {
        clearUserSession(userDetails, req, res);
    }

    // 회원 탈퇴
    public void withdrawUser(CustomUserDetails userDetails, HttpServletRequest req, HttpServletResponse res) {
        clearUserSession(userDetails, req, res);

        // 탈퇴 유예 기간(7일 -> mysql의 스케쥴러로 관리)
        LocalDateTime withdrawReqAt = LocalDateTime.now();

        if (userDetails.isUser()) {
            User user = userDetails.getUser();
            user.setWithdrawReqAt(withdrawReqAt);
            user.setWithdrawStatus(true);
            userRepository.save(user);
        } else {
            SnsUser snsUser = userDetails.getSnsUser();
            snsUser.setWithdrawReqAt(withdrawReqAt);
            snsUser.setWithdrawStatus(true);
            snsUserRepository.save(snsUser);
        }
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

    // 쿠키에 존재하는 토큰 삭제 무효화 및 레디스에 내역 삭제
    private void clearUserSession(CustomUserDetails userDetails, HttpServletRequest req, HttpServletResponse res) {
        String refreshKey = "refresh_" + userDetails.getUsername();
        String storedRefreshToken = Optional.ofNullable(redisTemplate.opsForValue().get(refreshKey))
                .map(token -> token.substring(7))
                .orElseThrow(() -> new IllegalArgumentException("Refresh token not found in Redis."));

        Optional<String> refreshTokenOpt = tokenCookieService.extractRefreshToken(req);
        refreshTokenOpt.ifPresent(r -> {
            if (!StringUtils.equals(r, storedRefreshToken)) {
                throw new IllegalArgumentException("Invalid refresh token.");
            }
        });

        redisTemplate.delete(refreshKey);
        tokenCookieService.clearTokens(res);
    }
}
