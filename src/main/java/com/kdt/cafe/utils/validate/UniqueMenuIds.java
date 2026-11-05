package com.kdt.cafe.utils.validate;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 주문 항목 목록에 같은 menuId가 두 번 이상 들어 있지 않은지 검사합니다.
 * items 필드에 붙이므로 오류가 나면 fieldErrors 이름이 명세대로 "items"가 됩니다.
 * null 목록은 통과시키므로 필수 여부는 @NotNull로 따로 검사합니다.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueMenuIdsValidator.class)
public @interface UniqueMenuIds {

    String message();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
