package com.kdt.cafe.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record OrderCreateRequestDTO(
        //@NotNull(message = "이메일은 필수입니다.")
        //@Size(min = 1, max = 100, message = "이메일은 1자 이상 100자 이하여야 합니다.")
        //@Pattern(regexp = EmailValidator.EMAIL_REGEX, message = "이메일 형식이 올바르지 않습니다.")
        String email,
        @NotBlank(message = "주소는 필수입니다.")
        @Size(max = 200, message = "주소는 200자 이하여야 합니다.")
        String address,

        @NotNull(message = "우편번호는 필수입니다.")
        @Pattern(regexp = "\\d{5}", message = "우편번호는 숫자 5자리여야 합니다.")
        String postcode,

        @NotNull(message = "주문 항목은 필수입니다.")
        @Size(min = 1, max = 10, message = "주문 항목은 1개 이상 10개 이하여야 합니다.")
        List<@Valid OrderItemRequestDTO> items
) {
}
