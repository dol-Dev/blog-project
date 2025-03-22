package com.doldev.dollog.global.validator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import com.doldev.dollog.domain.account.emailVerification.dto.req.EmailVerifyReqDto;
import com.doldev.dollog.domain.account.user.entity.User;
import com.doldev.dollog.domain.account.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class CheckEmailValidator extends AbstractValidator<EmailVerifyReqDto> {

    private final UserRepository userRepository;

    @Override
    protected void doValidate(EmailVerifyReqDto reqDto, Errors errors) {
        validateEmail(reqDto, errors);
    }

    private void validateEmail(EmailVerifyReqDto reqDto, Errors errors) {
        String email = reqDto.getEmail();
        if (StringUtils.isBlank(email)) {
            addError(errors, "email", "email.empty", "이메일은 필수 입력값입니다.");
            return;
        }
    
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            addError(errors, "email", "email.notfound", "등록된 이메일이 존재하지 않습니다.");
            return;
        }
    }
    
    // 공통 오류 추가 메서드
    private void addError(Errors errors, String field, String code, String message) {
        log.warn("유효성 검증 실패 - {}: {}", field, message);
        errors.rejectValue(field, code, message);
    }
}
