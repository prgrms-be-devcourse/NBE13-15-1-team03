package com.kdt.cafe.utils.validate;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * URL 날짜가 명세 범위(2000-01-01 ~ 2099-12-31) 안에 있는지 검사합니다.
 * 실제 날짜인지(2026-02-30 등)는 @DateTimeFormat 변환 단계에서 걸러집니다.
 * null은 통과시키므로 필수 여부는 @NotNull로 따로 검사합니다.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DateValidator.class)
public @interface ValidDate {

    String message();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
