package com.kdt.cafe.dto;

import jakarta.validation.constraints.*;

import java.util.List;

public record OrderCreateRequestDTO(
        String email,
        @NotBlank @Size(max = 200) String address,
        @NotNull @Pattern(regexp = "\\d{5}") String postcode,
        @NotNull @Size(min = 1, max = 10) List<OrderItemRequestDTO> items
) {
}
