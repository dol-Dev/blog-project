package com.doldev.dollog.global.validator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import com.doldev.dollog.domain.account.emailVerification.dto.req.EmailVerifyCodeReqDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class CheckEmailCodeValidator extends AbstractValidator<EmailVerifyCodeReqDto> {
    private final StringRedisTemplate redisTemplate;

    @Override
    protected void doValidate(EmailVerifyCodeReqDto reqDto, Errors errors) {
        validateVerifyCode(reqDto, errors);
    }

    private void validateVerifyCode(EmailVerifyCodeReqDto reqDto, Errors errors) {
        String code = reqDto.getCode();
        if (StringUtils.isBlank(code)) {
            addError(errors, "code", "code.empty", "인증 코드는 필수 입력값입니다.");
            return;
        }

        String storedCode = redisTemplate.opsForValue().get(code + "_send-verify-code");
        if (StringUtils.isBlank(storedCode)) {
            addError(errors, "code", "code.mismatch", "코드가 유효하지 않습니다.");
            return;
        }
    }

    // 공통 오류 추가 메서드
    private void addError(Errors errors, String field, String code, String message) {
        log.warn("유효성 검증 실패 - {}: {}", field, message);
        errors.rejectValue(field, code, message);
    }
}
