package com.doldev.dollog.global.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import com.doldev.dollog.domain.user.dto.req.SignupReqDto;
import com.doldev.dollog.domain.user.repository.UserRepository;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Component
public class CheckSignupValidator extends AbstractValidator<SignupReqDto> {

    private final UserRepository userRepository;

    private static final String USERNAME_PATTERN = "[a-zA-Z0-9]{5,13}";
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{1,}$";
    private static final String NICKNAME_PATTERN = "^[A-Za-z가-힣\\d!-/:-@\\[-`{-~]{2,8}$";

    @Override
    protected void doValidate(SignupReqDto req, Errors errors) {
        log.info("doValidate 실행: 사용자 {}", req.getUsername());

        validateEmail(req, errors);
        validateUsername(req, errors);
        validateNickname(req, errors);
    }

    // 이메일 검증
    private void validateEmail(SignupReqDto req, Errors errors) {
        if (StringUtils.isBlank(req.getEmail())) {
            addError(errors, "email", "필수 값 오류", "이메일은 필수 입력값입니다.");
            return;
        }

        // 이메일 형식 검증
        if (!req.getEmail().matches(EMAIL_PATTERN)) {
            addError(errors, "email", "형식 오류", "유효하지 않은 이메일 형식입니다.");
            return;
        }

        // 이메일 중복 체크
        if (userRepository.existsByEmail(req.getEmail())) {
            addError(errors, "email", "중복 오류", "이미 사용 중인 이메일입니다.");
        }
    }

    // 아이디 검증
    private void validateUsername(SignupReqDto req, Errors errors) {
        if (StringUtils.isBlank(req.getUsername())) {
            addError(errors, "username", "필수 값 오류", "아이디는 필수 입력값입니다.");
            return;
        }

        // 아이디 형식 검증
        if (!req.getUsername().matches(USERNAME_PATTERN)) {
            addError(errors, "username", "형식 오류", "아이디 형식에 맞지 않습니다. 아이디는 영문, 숫자의 조합으로 5 ~ 13자리여야 합니다.");
            return;
        }

        // 아이디 중복 체크
        if (userRepository.existsByUsername(req.getUsername())) {
            addError(errors, "username", "중복 오류", "이미 사용 중인 아이디입니다.");
        }
    }

    // 닉네임 검증
    private void validateNickname(SignupReqDto req, Errors errors) {
        if (StringUtils.isBlank(req.getNickname())) {
            addError(errors, "nickname", "필수 값 오류", "닉네임을 입력해주세요.");
            return;
        }

        // 닉네임 형식 검증
        if (!req.getNickname().matches(NICKNAME_PATTERN)) {
            addError(errors, "nickname", "형식 오류", "2~8자의 영문/한글/숫자/특수문자만 가능합니다.");
            return;
        }

        // 닉네임 중복 체크
        if (userRepository.existsByNickname(req.getNickname())) {
            addError(errors, "nickname", "중복 오류", "이미 사용 중인 닉네임입니다.");
        }
    }

    // 공통 오류 추가 메서드
    private void addError(Errors errors, String field, String code, String message) {
        log.warn("유효성 검증 실패 - {}: {}", field, message);
        errors.rejectValue(field, code, message);
    }
}
