package com.doldev.dollog.global.auth.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.doldev.dollog.global.auth.application.AuthService;
import com.doldev.dollog.global.auth.principal.CustomUserDetails;
import com.doldev.dollog.global.dto.ApiResDto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<ApiResDto<Void>> logout(@AuthenticationPrincipal CustomUserDetails userDetails,
            @CookieValue("accessToken") String accessToken,
            @CookieValue("refreshToken") String refreshToken) {
        authService.logout(userDetails, accessToken, refreshToken);
        return ResponseEntity.ok()
                .body(ApiResDto.<Void>builder()
                        .messageCode("LOGOUT_SUCCESS")
                        .build());
    }
}
