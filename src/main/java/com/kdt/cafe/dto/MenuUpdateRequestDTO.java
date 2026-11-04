package com.kdt.cafe.dto;

import jakarta.validation.constraints.*;

public record MenuUpdateRequestDTO(
        @NotNull @Min(1) Integer categoryId,
        @NotBlank @Size(max = 100) String name,
        @NotNull @Min(1) @Max(1_000_000) Integer price,
        @NotNull @Size(max = 300) String description
) {
}
