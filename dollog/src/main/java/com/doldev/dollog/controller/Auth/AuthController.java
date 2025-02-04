package com.doldev.dollog.controller.Auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.doldev.dollog.dto.api.ApiResDto;
import com.doldev.dollog.dto.auth.SignupReqDto;
import com.doldev.dollog.service.auth.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<ApiResDto<Void>> signup(@Valid @RequestBody SignupReqDto req) {
        authService.signup(req);
        return ResponseEntity.ok()
                .body(ApiResDto.<Void>builder()
                        .messageCode("SIGNUP_SUCCESS")
                        .build());
    }

}
