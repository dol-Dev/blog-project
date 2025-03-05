package com.doldev.dollog.global.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CheckUpdateAvatarValidator extends AbstractValidator<MultipartFile> {

    // 정규식 패턴 정의
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB 이하
    private static final String IMAGE_PATTERN = "image/.*"; // 이미지 파일 형식

    @Override
    protected void doValidate(MultipartFile avatarFile, Errors errors) {
        validateAvatarFile(avatarFile, errors);
    }

    private void validateAvatarFile(MultipartFile avatarFile, Errors errors) {
        // 공백 검증
        if (avatarFile == null || avatarFile.isEmpty()) {
            addError(errors, "avatarFile", "avatarFile.empty", "아바타 파일이 비어 있습니다.");
            return;
        }

        // 파일 크기 검증
        if (avatarFile.getSize() > MAX_FILE_SIZE) {
            addError(errors, "avatarFile", "avatarFile.size", "아바타 파일 크기는 5MB 이하이어야 합니다.");
            return;
        }

        // 파일 형식 검증
        String contentType = avatarFile.getContentType();
        if (contentType == null || !contentType.matches(IMAGE_PATTERN)) {
            addError(errors, "avatarFile", "avatarFile.type", "아바타 파일은 이미지 파일이어야 합니다.");
            return;
        }
    }

    // 공통 오류 추가 메서드
    private void addError(Errors errors, String field, String code, String message) {
        log.warn("유효성 검증 실패 - {}: {}", field, message);
        errors.rejectValue(field, code, message);
    }
}