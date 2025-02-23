package com.doldev.dollog.global.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import com.doldev.dollog.domain.account.profile.dto.req.BlogNameUpdateReqDto;

import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CheckUpdateBlogNameValidator extends AbstractValidator<BlogNameUpdateReqDto> {

    @Override
    protected void doValidate(BlogNameUpdateReqDto reqDto, Errors errors) {
        log.info("doValidate 실행: 블로그 이름 검증");

        if (StringUtils.isBlank(reqDto.getBlogName())) {
            addError(errors, "blogName", "blogName.empty", "블로그 이름을 입력해주세요.");
            return;
        }

        // 형식 검증 (예: 2~50자의 영문/한글/숫자/특수문자만 허용)
        if (!reqDto.getBlogName().matches("^[A-Za-z가-힣\\d!-/:-@\\[-`{-~]{2,50}$")) {
            addError(errors, "blogName", "blogName.format", "2~50자의 영문/한글/숫자/특수문자만 가능합니다.");
        }
    }

    // 공통 오류 추가 메서드
    private void addError(Errors errors, String field, String code, String message) {
        log.warn("유효성 검증 실패 - {}: {}", field, message);
        errors.rejectValue(field, code, message);
    }
}