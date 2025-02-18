package com.doldev.dollog.global.validator;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public abstract class AbstractValidator<T> implements Validator {

    // Validator가 주어진 클래스에 대한 유효성 검사를 지원하는지 확인함.
    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return true;
    }

    // 유효성 검사 진행
    @SuppressWarnings("unchecked")
    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        try {
            doValidate((T) target, errors);
        } catch (RuntimeException e) {
            throw e;
        }
    }

    @Override
    public @NonNull Errors validateObject(@NonNull Object target) {
        return Validator.super.validateObject(target);
    }

    // for CheckSignupValidator,(CheckUserUpdateValidator는 곧 구현할 예정)
    protected abstract void doValidate(final T dto, final Errors errors);
}
