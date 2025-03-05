package com.doldev.dollog.global.validator;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import com.doldev.dollog.domain.account.profile.dto.req.NicknameUpdateReqDto;
import com.doldev.dollog.domain.account.profile.entity.Profile;
import com.doldev.dollog.domain.account.profile.repository.ProfileRepository;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Component
public class CheckUpdateNicknameValidator extends AbstractValidator<NicknameUpdateReqDto> {

    private final ProfileRepository profileRepository;

    // 정규식 패턴 정의
    private static final String NICKNAME_PATTERN = "^[A-Za-z가-힣\\d!-/:-@\\[-`{-~]{2,13}$"; // 2~13자, 특수문자 포함 가능
    private static final String SPACE_PATTERN = "\\s"; // 공백 포함 여부

    @Override
    protected void doValidate(NicknameUpdateReqDto reqDto, Errors errors) {
        Profile profile = profileRepository.findByNickname(reqDto.getNewNickname()).orElse(null);
        validateNickname(reqDto, profile, errors);
    }

    // 닉네임 검증
    private void validateNickname(NicknameUpdateReqDto reqDto, Profile profile, Errors errors) {

        // 동일한 닉네임일 때 검증 패스
        if (profile != null && reqDto.getNewNickname().equals(profile.getNickname())) {
            return;
        }

        // 공백 검증
        if (StringUtils.isBlank(reqDto.getNewNickname())) {
            addError(errors, "newNickname", "newNickname.empty", "닉네임을 입력해주세요.");
            return;
        }

        // 형식 검증(1)
        if (Pattern.matches(SPACE_PATTERN, reqDto.getNewNickname())) {
            addError(errors, "newNickname", "newNickname.space", "닉네임은 공백을 포함할 수 없습니다.");
            return;
        }

        // 형식 검증(2)
        if (!reqDto.getNewNickname().matches(NICKNAME_PATTERN)) {
            addError(errors, "newNickname", "newNickname.format", "2~13자의 영문/한글/숫자/특수문자만 가능합니다.");
            return;
        }

        // 중복 검증
        if (profileRepository.existsByNickname(reqDto.getNewNickname())) {
            addError(errors, "newNickname", "newNickname.duplicate", "이미 사용 중인 닉네임입니다.");
            return;
        }
    }

    // 공통 오류 추가 메서드
    private void addError(Errors errors, String field, String code, String message) {
        log.warn("유효성 검증 실패 - {}: {}", field, message);
        errors.rejectValue(field, code, message);
    }
}