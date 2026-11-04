package com.kdt.cafe.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OrderMemoUpdateRequestDTO(
        @NotNull @Size(max = 200) String memo
) {
}
