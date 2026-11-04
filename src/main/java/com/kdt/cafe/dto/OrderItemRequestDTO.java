package com.kdt.cafe.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequestDTO(
        @NotNull Integer menuId,
        @NotNull @Min(1) @Max(20) Integer quantity
) {
}
