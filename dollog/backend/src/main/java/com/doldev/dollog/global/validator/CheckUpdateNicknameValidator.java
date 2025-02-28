package com.doldev.dollog.global.validator;

import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import com.doldev.dollog.domain.account.profile.dto.req.NicknameUpdateReqDto;
import com.doldev.dollog.domain.account.profile.entity.Profile;
import com.doldev.dollog.domain.account.profile.repository.ProfileRepository;
import com.doldev.dollog.domain.account.user.entity.User;
import com.doldev.dollog.domain.account.user.repository.UserRepository;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Component
public class CheckUpdateNicknameValidator extends AbstractValidator<NicknameUpdateReqDto> {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private Optional<User> userOpt;

    // 정규식 패턴 정의
    private static final String NICKNAME_PATTERN = "^[A-Za-z가-힣\\d!-/:-@\\[-`{-~]{2,8}$"; // 2~8자, 특수문자 포함 가능
    private static final String SPACE_PATTERN = "\\s"; // 공백 포함 여부

    @Override
    protected void doValidate(NicknameUpdateReqDto reqDto, Errors errors) {
        log.info("doValidate 실행: 사용자 {}", reqDto.getUsername());

        userOpt = userRepository.findByUsername(reqDto.getUsername());
        if (!userOpt.isPresent()) {
            addError(errors, "username", "user.notFound", "사용자를 찾을 수 없습니다.");
            return;
        }

        Profile profile = userOpt.get().getProfile();
        validateNickname(reqDto, profile, errors);
    }

    // 닉네임 검증
    private void validateNickname(NicknameUpdateReqDto reqDto, Profile profile, Errors errors) {

        if (StringUtils.isBlank(reqDto.getNickname())) {
            addError(errors, "nickname", "nickname.empty", "닉네임을 입력해주세요.");
            return;
        }

        // 형식 검증
        if (Pattern.matches(SPACE_PATTERN, reqDto.getNickname())) {
            addError(errors, "nickname", "nickname.space", "닉네임은 공백을 포함할 수 없습니다.");
        }
        if (!reqDto.getNickname().matches(NICKNAME_PATTERN)) {
            addError(errors, "nickname", "nickname.format", "2~8자의 영문/한글/숫자/특수문자만 가능합니다.");
            return;
        }

        // 중복 검증
        if (profileRepository.existsByNickname(reqDto.getNickname())) {
            addError(errors, "nickname", "nickname.duplicate", "이미 사용 중인 닉네임입니다.");
        }
    }

    // 공통 오류 추가 메서드
    private void addError(Errors errors, String field, String code, String message) {
        log.warn("유효성 검증 실패 - {}: {}", field, message);
        errors.rejectValue(field, code, message);
    }
}