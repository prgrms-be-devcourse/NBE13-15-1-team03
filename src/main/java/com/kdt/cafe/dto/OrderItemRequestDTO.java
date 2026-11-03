package com.kdt.cafe.dto;

public record OrderItemRequestDTO(
        int menuId,
        int quantity
) {
}
