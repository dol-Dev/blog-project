package com.doldev.dollog.global.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import com.doldev.dollog.domain.account.profile.dto.req.BlogNameUpdateReqDto;

import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CheckUpdateBlogNameValidator extends AbstractValidator<BlogNameUpdateReqDto> {

    // 정규식 패턴 정의
    private static final String BLOGNAME_PATTERN = "^[A-Za-z가-힣\\d!-/:-@\\[-`{-~]{2,50}$"; // 2~50자, 특수문자 포함 가능

    @Override
    protected void doValidate(BlogNameUpdateReqDto reqDto, Errors errors) {
        log.info("doValidate 실행: 블로그 이름 검증");

        if (StringUtils.isBlank(reqDto.getBlogName())) {
            addError(errors, "blogName", "blogName.empty", "블로그 이름을 입력해주세요.");
            return;
        }

        if (!reqDto.getBlogName().matches(BLOGNAME_PATTERN)) {
            addError(errors, "blogName", "blogName.format", "2~50자의 영문/한글/숫자/특수문자만 가능합니다.");
        }
    }

    // 공통 오류 추가 메서드
    private void addError(Errors errors, String field, String code, String message) {
        log.warn("유효성 검증 실패 - {}: {}", field, message);
        errors.rejectValue(field, code, message);
    }
}