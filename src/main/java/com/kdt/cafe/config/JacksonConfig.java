package com.kdt.cafe.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.cfg.CoercionAction;
import tools.jackson.databind.cfg.CoercionInputShape;
import tools.jackson.databind.type.LogicalType;

/*
 * 요청 JSON 자료형 검사
 * - Jackson은 기본적으로 값을 알아서 변환함 (예: "true" → true, 123 → "123", 1.5 → 1, "5000" → 5000)
 * - 명세: 잘못된 자료형은 400, 판매 여부는 JSON boolean만 허용
 * - 변환을 막으면 Jackson 오류가 나고, ApiExceptionAdvice가 400 VALIDATION_ERROR(fieldErrors에 해당 필드)로 응답함
 */
@Configuration
public class JacksonConfig {

    @Bean
    public JsonMapperBuilderCustomizer strictTypeCoercion() {
        return builder -> builder
                // 문자열 필드(name·email·address·postcode·memo 등)에 숫자·불리언이 오면 거절
                .withCoercionConfig(LogicalType.Textual, config -> config
                        .setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)
                        .setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
                        .setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail))
                // 정수 필드(ID·가격·수량)에 문자열·소수가 오면 거절
                .withCoercionConfig(LogicalType.Integer, config -> config
                        .setCoercion(CoercionInputShape.String, CoercionAction.Fail)
                        .setCoercion(CoercionInputShape.Float, CoercionAction.Fail))
                // 불리언 필드(available)에 문자열·숫자가 오면 거절
                .withCoercionConfig(LogicalType.Boolean, config -> config
                        .setCoercion(CoercionInputShape.String, CoercionAction.Fail)
                        .setCoercion(CoercionInputShape.Integer, CoercionAction.Fail));
    }
}
