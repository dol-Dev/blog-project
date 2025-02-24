package com.doldev.dollog.global.handler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.doldev.dollog.global.dto.ApiResDto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ControllerAdvice
public class ValidateHandler {

    // 유효성 검증 실패 처리
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResDto<?>> handleBindException(BindException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fieldError -> {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        });
        return ResponseEntity.badRequest()
                .body(ApiResDto.builder()
                        .messageCode("FIELD_VALIDATION_ERROR")
                        .errors(errors)
                        .build());
    }

    // DB 예외 처리
    @ExceptionHandler(JpaSystemException.class)
    public ResponseEntity<ApiResDto<?>> handleDatabaseException(JpaSystemException ex) {
        return ResponseEntity.internalServerError()
                .body(ApiResDto.builder()
                        .messageCode("DATABASE_ERROR")
                        .build());
    }
}
