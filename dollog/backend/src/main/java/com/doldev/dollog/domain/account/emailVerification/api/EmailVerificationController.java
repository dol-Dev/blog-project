package com.doldev.dollog.domain.account.emailVerification.api;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.doldev.dollog.domain.account.emailVerification.application.EmailVerificationService;
import com.doldev.dollog.domain.account.emailVerification.dto.req.EmailVerifyCodeReqDto;
import com.doldev.dollog.domain.account.emailVerification.dto.req.EmailVerifyReqDto;
import com.doldev.dollog.global.dto.ApiResDto;
import com.doldev.dollog.global.validator.CheckEmailCodeValidator;
import com.doldev.dollog.global.validator.CheckEmailValidator;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/codes")
@RequiredArgsConstructor
public class EmailVerificationController {
    private final EmailVerificationService emailService;
    private final StringRedisTemplate redisTemplate;
    private final CheckEmailValidator checkEmailValidator;
    private final CheckEmailCodeValidator checkEmailCodeValidator;

    // 인증코드 발급
    @PostMapping
    public ResponseEntity<ApiResDto<?>> sendCodeMail(@RequestBody EmailVerifyReqDto reqDto) throws BindException {
        BindingResult bindingResult = new BeanPropertyBindingResult(reqDto, "emailVerifyReqDto");
        checkEmailValidator.validate(reqDto, bindingResult);
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        String code = emailService.sendCodeMail(reqDto.getEmail());
        redisTemplate.opsForValue().set(code + "_send-verify-code", code, 3, TimeUnit.MINUTES);
        return ResponseEntity.ok(ApiResDto.builder()
                .messageCode("CODE_SEND_SUCCESS")
                .data(true)
                .build());
    }

    // 인증코드 검증
    @PostMapping("/verify")
    public ResponseEntity<ApiResDto<?>> verifyCode(@RequestBody EmailVerifyCodeReqDto reqDto) throws BindException {
        BindingResult bindingResult = new BeanPropertyBindingResult(reqDto, "emailVerifyCodeReqDto");
        checkEmailCodeValidator.validate(reqDto, bindingResult);
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        redisTemplate.delete(reqDto.getCode() + "_send-verify-code");
        return ResponseEntity.ok(ApiResDto.builder()
                .messageCode("CODE_VERIFY_SUCCESS")
                .build());
    }
}
