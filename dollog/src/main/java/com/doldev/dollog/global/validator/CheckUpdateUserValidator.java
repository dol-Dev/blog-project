package com.doldev.dollog.global.validator;

import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import com.doldev.dollog.domain.account.user.dto.req.UpdateUserReqDto;
import com.doldev.dollog.domain.account.user.entity.User;
import com.doldev.dollog.domain.account.user.repository.UserRepository;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Component
public class CheckUpdateUserValidator extends AbstractValidator<UpdateUserReqDto> {

    private final UserRepository userRepository;
    private Optional<User> optionalUser;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    protected void doValidate(UpdateUserReqDto reqDto, Errors errors) {
        log.info("doValidate 실행: 사용자 {}", reqDto.getUsername());

        optionalUser = userRepository.findByUsername(reqDto.getUsername());

        if (!optionalUser.isPresent()) {
            errors.reject("user.notFound", "사용자를 찾을 수 없습니다.");
            return;
        }

        validateEmail(reqDto, errors);
        validatePassword(reqDto, errors);
    }

    // 이메일 검증
    private void validateEmail(UpdateUserReqDto reqDto, Errors errors) {

        if (StringUtils.isBlank(reqDto.getEmail())) {
            addError(errors, "email", "필수 값 오류", "이메일은 필수 입력값입니다.");
            return;
        }

        if (!reqDto.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{1,}$")) {
            addError(errors, "email", "형식 오류", "유효하지 않은 이메일 형식입니다.");
            return;
        }

        User currentUser = optionalUser.get();
        if (!reqDto.getEmail().equals(currentUser.getEmail())
                && userRepository.existsByEmail(reqDto.getEmail())) {
            addError(errors, "email", "중복 오류", "이미 사용 중인 이메일입니다.");
        }
    }

    // 비밀번호 검증
    private void validatePassword(UpdateUserReqDto reqDto, Errors errors) {

        // 기존 비밀번호 확인
        User currentUser = optionalUser.get();
        if (StringUtils.isBlank(reqDto.getCurrentPassword())
                || !passwordEncoder.matches(reqDto.getCurrentPassword(), currentUser.getPassword())) {
            addError(errors, "password", "불일치 오류", "기존 비밀번호가 일치하지 않습니다.");
            return;
        }

        // 새 비밀번호 유효성 검사
        if (StringUtils.isNotBlank(reqDto.getNewPassword())) {
            if (!reqDto.getNewPassword()
                    .matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!-/:-@\\[-`{-~])[A-Za-z\\d!-/:-@\\[-`{-~]{8,16}$")) {
                addError(errors, "NewPassword", "형식 오류", "영문+숫자+특수문자 조합 8~16자로 입력해주세요.");
                return;
            }
        }
    }

    // 공통 오류 추가 메서드
    private void addError(Errors errors, String field, String code, String message) {
        log.warn("유효성 검증 실패 - {}: {}", field, message);
        errors.rejectValue(field, code, message);
    }
}