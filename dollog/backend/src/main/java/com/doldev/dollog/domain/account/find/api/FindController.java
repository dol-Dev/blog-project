package com.doldev.dollog.domain.account.find.api;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.doldev.dollog.domain.account.find.application.FindService;
import com.doldev.dollog.global.dto.ApiResDto;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/accounts/recovery")
public class FindController {
    private final FindService findService;
    private final StringRedisTemplate redisTemplate;

    // 인증코드 발급
    @PostMapping("/code")
    public ResponseEntity<ApiResDto<?>> sendCodeMail(@RequestParam("email") String email) {
        String code = findService.sendCodeMail(email);
        if (StringUtils.isNotBlank(code)) {
            redisTemplate.opsForValue().set(code + "_send-verify-code", code, 3, TimeUnit.MINUTES);
            return ResponseEntity.ok().body(ApiResDto.builder().messageCode("인증코드 발송 성공!").data(true).build());
        }
        return ResponseEntity.badRequest().body(ApiResDto.builder().messageCode("인증코드 발송 실패").data(false).build());
    }

    // 인증코드 검증
    @PostMapping("/code/verify")
    public ResponseEntity<ApiResDto<?>> verifyCode(@RequestParam("code") String code) {
        String validCode = redisTemplate.opsForValue().get(code + "_send-verify-code");
        if (StringUtils.isNotBlank(validCode)) {
            redisTemplate.delete(code + "_send_verify-code");
            return ResponseEntity.ok().body(ApiResDto.builder().messageCode("인증코드 검증 성공!").data(true).build());
        } else {
            return ResponseEntity.badRequest().body(ApiResDto.builder().messageCode("인증코드 검증 실패").data(false).build());
        }
    }

    // 비밀번호 찾기(임시 비밀번호 발급)
    @PostMapping("/password")
    public ResponseEntity<ApiResDto<?>> sendTempPasswordMail(@RequestParam("email") String email) {
        String tempPassword = findService.sendTempPasswordMail(email);
        if (StringUtils.isNotBlank(tempPassword)) {
            return ResponseEntity.ok().body(ApiResDto.builder().messageCode("임시 비밀번호 발급 성공!").data(true).build());
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResDto.builder().messageCode("임시 비밀번호 발급 실패").data(false).build());
        }
    }

    // 아이디 찾기
    @PostMapping("/username")
    public ResponseEntity<ApiResDto<?>> sendUsernameMail(@RequestParam("email") String email) {
        String username = findService.sendUsernameMail(email);
        if (StringUtils.isNotBlank(username)) {
            return ResponseEntity.ok().body(ApiResDto.builder().messageCode("아이디 찾기 성공!").data(true).build());
        } else {
            return ResponseEntity.badRequest().body(ApiResDto.builder().messageCode("아이디 찾기 실패").data(false).build());
        }
    }
}
