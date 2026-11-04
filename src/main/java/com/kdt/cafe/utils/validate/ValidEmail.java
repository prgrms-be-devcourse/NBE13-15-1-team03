package com.kdt.cafe.utils.validate;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 이메일이 명세 정규식에 문자열 전체가 일치하는지 검사합니다. (공백 제거 없음, 대소문자 구분)
 * null은 통과시키므로 필수 여부는 @NotNull, 길이는 @Size로 따로 검사합니다.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EmailValidator.class)
public @interface ValidEmail {

    String message();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
