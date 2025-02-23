package com.doldev.dollog.global.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CheckUpdateAvatarValidator extends AbstractValidator<MultipartFile> {

    @Override
    protected void doValidate(MultipartFile avatarFile, Errors errors) {
        log.info("doValidate 실행: 아바타 파일 검증");

        if (avatarFile == null || avatarFile.isEmpty()) {
            addError(errors, "avatarFile", "avatarFile.empty", "아바타 파일이 비어 있습니다.");
            return;
        }

        // 파일 크기 검증 (예: 5MB 이하)
        if (avatarFile.getSize() > 5 * 1024 * 1024) {
            addError(errors, "avatarFile", "avatarFile.size", "아바타 파일 크기는 5MB 이하이어야 합니다.");
        }

        // 파일 형식 검증 (예: 이미지 파일만 허용)
        String contentType = avatarFile.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            addError(errors, "avatarFile", "avatarFile.type", "아바타 파일은 이미지 파일이어야 합니다.");
        }
    }

    // 공통 오류 추가 메서드
    private void addError(Errors errors, String field, String code, String message) {
        log.warn("유효성 검증 실패 - {}: {}", field, message);
        errors.rejectValue(field, code, message);
    }
}