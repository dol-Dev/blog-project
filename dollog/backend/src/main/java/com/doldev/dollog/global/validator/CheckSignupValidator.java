package com.doldev.dollog.global.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import com.doldev.dollog.domain.account.profile.repository.ProfileRepository;
import com.doldev.dollog.domain.account.user.dto.req.UserSignupReqDto;
import com.doldev.dollog.domain.account.user.repository.UserRepository;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Slf4j
@Component
public class CheckSignupValidator extends AbstractValidator<UserSignupReqDto> {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    // 정규식 패턴 정의
    private static final String USERNAME_PATTERN = "^[a-zA-Z0-9]{5,13}$"; // 영문, 숫자 5~13자
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{1,}$"; // 이메일 형식
    private static final String NICKNAME_PATTERN = "^[A-Za-z가-힣\\d!-/:-@\\[-`{-~]{2,8}$"; // 2~8자, 특수문자 포함 가능
    private static final String PASSWORD_PATTERN = "^(?=.*[a-zA-Z])(?=.*[0-9])|(?=.*[a-zA-Z])(?=.*[@#$%^&+=!])|(?=.*[0-9])(?=.*[@#$%^&+=!]).{8,20}$"; // 비밀번호 복잡도 (3가지 포함)
    private static final String HANGUL_PATTERN = ".*[ㄱ-ㅎㅏ-ㅣ가-힣].*"; // 한글 포함 여부
    private static final String SPACE_PATTERN = "\\s"; // 공백 포함 여부

    @Override
    protected void doValidate(UserSignupReqDto req, Errors errors) {
        log.info("doValidate 실행: 사용자 {}", req.getUsername());

        validateUsername(req, errors);
        validateEmail(req, errors);
        validateNickname(req, errors);
        validatePassword(req, errors);
    }

    // 아이디(Username) 검증
    private void validateUsername(UserSignupReqDto req, Errors errors) {
        if (StringUtils.isBlank(req.getUsername())) {
            addError(errors, "username", "username.empty", "아이디는 필수 입력값입니다.");
            return;
        }
        if (Pattern.matches(SPACE_PATTERN, req.getUsername())) {
            addError(errors, "username", "username.space", "아이디는 공백을 포함할 수 없습니다.");
        }
        if (Pattern.matches(HANGUL_PATTERN, req.getUsername())) {
            addError(errors, "username", "username.hangul", "아이디에 한글을 포함할 수 없습니다.");
        }
        if (!req.getUsername().matches(USERNAME_PATTERN)) {
            addError(errors, "username", "username.format", "아이디 형식에 맞지 않습니다. 아이디는 영문, 숫자의 조합으로 5 ~ 13자리여야 합니다.");
        }
        if (userRepository.existsByUsername(req.getUsername())) {
            addError(errors, "username", "username.duplicate", "이미 사용 중인 아이디입니다.");
        }
    }

    // 이메일 검증
    private void validateEmail(UserSignupReqDto req, Errors errors) {
        if (StringUtils.isBlank(req.getEmail())) {
            addError(errors, "email", "email.empty", "이메일은 필수 입력값입니다.");
            return;
        }
        if (Pattern.matches(SPACE_PATTERN, req.getEmail())) {
            addError(errors, "email", "email.space", "이메일은 공백을 포함할 수 없습니다.");
        }
        if (Pattern.matches(HANGUL_PATTERN, req.getEmail())) {
            addError(errors, "email", "email.hangul", "이메일에 한글을 포함할 수 없습니다.");
        }
        if (!req.getEmail().matches(EMAIL_PATTERN)) {
            addError(errors, "email", "email.format", "유효하지 않은 이메일 형식입니다.");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            addError(errors, "email", "email.duplicate", "이미 사용 중인 이메일입니다.");
        }
    }

    // 닉네임 검증
    private void validateNickname(UserSignupReqDto req, Errors errors) {
        if (StringUtils.isBlank(req.getNickname())) {
            addError(errors, "nickname", "nickname.empty", "닉네임을 입력해주세요.");
            return;
        }
        if (Pattern.matches(SPACE_PATTERN, req.getNickname())) {
            addError(errors, "nickname", "nickname.space", "닉네임은 공백을 포함할 수 없습니다.");
        }
        if (!req.getNickname().matches(NICKNAME_PATTERN)) {
            addError(errors, "nickname", "nickname.format", "2~8자의 영문/한글/숫자/특수문자만 가능합니다.");
        }
        if (profileRepository.existsByNickname(req.getNickname())) {
            addError(errors, "nickname", "nickname.duplicate", "이미 사용 중인 닉네임입니다.");
        }
    }

    // 비밀번호 검증
    private void validatePassword(UserSignupReqDto req, Errors errors) {
        if (StringUtils.isBlank(req.getPassword())) {
            addError(errors, "password", "password.empty", "비밀번호는 필수 입력값입니다.");
            return;
        }
        if (Pattern.matches(SPACE_PATTERN, req.getPassword())) {
            addError(errors, "password", "password.space", "비밀번호는 공백을 포함할 수 없습니다.");
        }
        if (Pattern.matches(HANGUL_PATTERN, req.getPassword())) {
            addError(errors, "password", "password.hangul", "비밀번호에 한글을 사용할 수 없습니다.");
        }
        if (!req.getPassword().matches(PASSWORD_PATTERN)) {
            addError(errors, "password", "password.format", "비밀번호는 8~20자이며, 소문자/대문자/숫자/특수문자 중 3가지 이상 포함해야 합니다.");
        }
    }

    // 공통 오류 추가 메서드
    private void addError(Errors errors, String field, String code, String message) {
        log.warn("유효성 검증 실패 - {}: {}", field, message);
        errors.rejectValue(field, code, message);
    }
}
