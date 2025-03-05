package com.doldev.dollog.global.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@SuppressWarnings("null")
@Component
public abstract class AbstractValidator<T> implements Validator {

    // Validator가 주어진 클래스에 대한 유효성 검사를 지원하는지 확인함.
    @Override
    public boolean supports(Class<?> clazz) {
        return true;
    }

    // 유효성 검사 진행
    @SuppressWarnings("unchecked")
    @Override
    public void validate(Object target, Errors errors) {
        try {
            doValidate((T) target, errors);
        } catch (RuntimeException e) {
            throw e;
        }
    }

    @Override
    public Errors validateObject(Object target) {
        return Validator.super.validateObject(target);
    }

    protected abstract void doValidate(final T dto, final Errors errors);
}
