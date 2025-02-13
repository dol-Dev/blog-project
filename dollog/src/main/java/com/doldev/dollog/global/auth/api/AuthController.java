package com.doldev.dollog.global.auth.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.doldev.dollog.domain.account.user.dto.req.LoginReqDto;
import com.doldev.dollog.global.auth.application.AuthService;
import com.doldev.dollog.global.auth.principal.CustomUserDetails;
import com.doldev.dollog.global.dto.ApiResDto;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

        private final AuthService authService;

        // 로그인
        @PostMapping("/login")
        public ResponseEntity<ApiResDto<Void>> login(
                        @RequestBody LoginReqDto reqDto,
                        HttpServletResponse res) {
                authService.login(reqDto, res);
                return ResponseEntity.ok(
                                ApiResDto.<Void>builder().messageCode("SUCCESS_LOGIN").build());
        }

        // 로그아웃
        @PostMapping("/logout")
        public ResponseEntity<ApiResDto<Void>> logout(@AuthenticationPrincipal CustomUserDetails userDetails,
                        @CookieValue("accessToken") String accessToken,
                        @CookieValue("refreshToken") String refreshToken,
                        HttpServletResponse res) {
                authService.logout(userDetails, accessToken, refreshToken, res);
                return ResponseEntity.ok()
                                .body(ApiResDto.<Void>builder()
                                                .messageCode("LOGOUT_SUCCESS")
                                                .build());
        }

        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<ApiResDto<Void>> handleAuthException(AuthenticationException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(ApiResDto.<Void>builder().messageCode("LOGIN_FAILED").build());
        }
}
