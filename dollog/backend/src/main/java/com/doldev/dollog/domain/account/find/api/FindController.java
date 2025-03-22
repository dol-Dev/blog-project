package com.doldev.dollog.domain.account.find.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.doldev.dollog.domain.account.find.application.FindService;
import com.doldev.dollog.global.dto.ApiResDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts/recovery")
public class FindController {
    private final FindService findService;

    // 비밀번호 찾기(임시 비밀번호 발급)
    @PostMapping("/password")
    public ResponseEntity<ApiResDto<?>> sendTempPasswordMail(@RequestParam("email") String email) {
        findService.sendTempPasswordMail(email);
        return ResponseEntity.ok(ApiResDto.builder()
                .messageCode("TEMP_PASSWORD_SEND_SUCCESS")
                .build());
    }

    // 아이디 찾기
    @PostMapping("/username")
    public ResponseEntity<ApiResDto<?>> sendUsernameMail(@RequestParam("email") String email) {
        findService.sendUsernameMail(email);
        return ResponseEntity.ok(ApiResDto.builder()
                .messageCode("USERNAME_SEND_SUCCESS")
                .build());
    }
}
