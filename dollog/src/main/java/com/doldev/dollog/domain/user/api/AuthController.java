package com.doldev.dollog.domain.user.api;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.doldev.dollog.domain.user.application.AuthService;
import com.doldev.dollog.domain.user.dto.req.SignupReqDto;
import com.doldev.dollog.global.dto.ApiResDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Controller
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
