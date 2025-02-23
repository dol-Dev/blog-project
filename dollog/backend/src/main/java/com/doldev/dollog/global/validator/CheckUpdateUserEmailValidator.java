package com.doldev.dollog.global.validator;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import com.doldev.dollog.domain.account.user.dto.req.EmailUpdateReqDto;
import com.doldev.dollog.domain.account.user.entity.User;
import com.doldev.dollog.domain.account.user.repository.UserRepository;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Component
public class CheckUpdateUserEmailValidator extends AbstractValidator<EmailUpdateReqDto> {

    private final UserRepository userRepository;
    private Optional<User> optionalUser;

    @Override
    protected void doValidate(EmailUpdateReqDto reqDto, Errors errors) {
        log.info("doValidate 실행: 이메일 업데이트 사용자 {}", reqDto.getUsername());

        optionalUser = userRepository.findByUsername(reqDto.getUsername());

        if (!optionalUser.isPresent()) {
            addError(errors, "username", "user.notFound", "사용자를 찾을 수 없습니다.");
            return;
        }

        validateEmail(reqDto, errors);
    }

    // 이메일 검증
    private void validateEmail(EmailUpdateReqDto reqDto, Errors errors) {

        if (StringUtils.isBlank(reqDto.getNewEmail())) {
            addError(errors, "email", "email.empty", "이메일은 필수 입력값입니다.");
            return;
        }

        if (!reqDto.getNewEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{1,}$")) {
            addError(errors, "email", "email.format", "유효하지 않은 이메일 형식입니다.");
            return;
        }

        User currentUser = optionalUser.get();
        if (!reqDto.getNewEmail().equals(currentUser.getEmail())
                && userRepository.existsByEmail(reqDto.getNewEmail())) {
            addError(errors, "email", "email.duplicate", "이미 사용 중인 이메일입니다.");
        }
    }

    // 공통 오류 추가 메서드
    private void addError(Errors errors, String field, String code, String message) {
        log.warn("유효성 검증 실패 - {}: {}", field, message);
        errors.rejectValue(field, code, message);
    }
}
