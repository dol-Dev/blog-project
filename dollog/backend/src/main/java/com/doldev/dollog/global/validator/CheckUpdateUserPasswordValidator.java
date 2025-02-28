package com.doldev.dollog.global.validator;

import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import com.doldev.dollog.domain.account.user.dto.req.PasswordUpdateReqDto;
import com.doldev.dollog.domain.account.user.entity.User;
import com.doldev.dollog.domain.account.user.repository.UserRepository;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Component
public class CheckUpdateUserPasswordValidator extends AbstractValidator<PasswordUpdateReqDto> {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private Optional<User> userOpt;

    // 정규식 패턴 정의
    private static final String PASSWORD_PATTERN = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!-/:-@\\[-`{-~])[A-Za-z\\d!-/:-@\\[-`{-~]{8,16}$"; // 영문+숫자+특수문자 조합 8~16자
    private static final String SPACE_PATTERN = "\\s"; // 공백 포함 여부

    @Override
    protected void doValidate(PasswordUpdateReqDto reqDto, Errors errors) {
        log.info("doValidate 실행: 비밀번호 업데이트 사용자 {}", reqDto.getUsername());

        userOpt = userRepository.findByUsername(reqDto.getUsername());

        if (!userOpt.isPresent()) {
            addError(errors, "username", "user.notFound", "사용자를 찾을 수 없습니다.");
            return;
        }
        validatePassword(reqDto, errors);
    }

    // 비밀번호 검증
    private void validatePassword(PasswordUpdateReqDto reqDto, Errors errors) {

        // 기존 비밀번호 확인
        if (StringUtils.isBlank(reqDto.getCurrentPassword())
                || !passwordEncoder.matches(reqDto.getCurrentPassword(), userOpt.get().getPassword())) {
            addError(errors, "currentPassword", "password.mismatch", "기존 비밀번호가 일치하지 않습니다.");
            return;
        }

        // 새 비밀번호 유효성 검사
        if (StringUtils.isNotBlank(reqDto.getNewPassword())) {
            if (Pattern.matches(SPACE_PATTERN, reqDto.getNewPassword())) {
                addError(errors, "newPassword", "password.space", "비밀번호는 공백을 포함할 수 없습니다.");
            }
            if (!reqDto.getNewPassword().matches(PASSWORD_PATTERN)) {
                addError(errors, "newPassword", "password.format", "영문+숫자+특수문자 조합 8~16자로 입력해주세요.");
            }
        }
    }

    // 공통 오류 추가 메서드
    private void addError(Errors errors, String field, String code, String message) {
        log.warn("유효성 검증 실패 - {}: {}", field, message);
        errors.rejectValue(field, code, message);
    }
}