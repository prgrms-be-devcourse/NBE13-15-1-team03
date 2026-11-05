package com.kdt.cafe.dto.order;

import com.kdt.cafe.utils.validate.ValidEmail;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OrderListRequestDTO(
        @NotNull(message = "이메일이 비어있습니다")
        @Size(min = 1, max = 100, message = "길이 제한을 벗어났습니다")
        @ValidEmail(message = "이메일이 형식에 맞지 않습니다")
        String email
) {
}