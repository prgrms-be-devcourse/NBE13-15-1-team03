package com.kdt.cafe.dto;

import jakarta.validation.constraints.NotNull;

public record MenuAvailabilityUpdateRequestDTO(
        @NotNull Boolean available
) {
}
