package com.kdt.cafe.dto;

import jakarta.validation.constraints.NotNull;

public record MenuAvailabilityUpdateRequestDTO(
        @NotNull(message = "판매 여부를 입력해 주세요.") Boolean available
) {
}
