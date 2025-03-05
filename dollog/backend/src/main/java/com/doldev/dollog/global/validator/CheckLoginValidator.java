package com.doldev.dollog.global.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import com.doldev.dollog.domain.account.user.dto.req.UserLoginReqDto;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class CheckLoginValidator extends AbstractValidator<UserLoginReqDto> {

    @Override
    protected void doValidate(UserLoginReqDto req, Errors errors) {
        // 공백 검증 메서드
        validateUsername(req, errors);
        validatePassword(req, errors);
    }

    private void validateUsername(UserLoginReqDto req, Errors errors) {
        if (StringUtils.isBlank(req.getUsername())) {
            addError(errors, "username", "username.empty", "아이디는 필수 입력값입니다.");
        }
    }

    private void validatePassword(UserLoginReqDto req, Errors errors) {
        if (StringUtils.isBlank(req.getPassword())) {
            addError(errors, "password", "password.empty", "비밀번호는 필수 입력값입니다.");
        }
    }

    // 공통 오류 추가 메서드
    private void addError(Errors errors, String field, String code, String message) {
        log.warn("유효성 검증 실패 - {}: {}", field, message);
        errors.rejectValue(field, code, message);
    }
}
