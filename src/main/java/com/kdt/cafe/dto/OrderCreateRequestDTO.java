package com.kdt.cafe.dto;

import java.util.List;

public record OrderCreateRequestDTO(
        String email,
        String address,
        String postcode,
        String memo,
        List<OrderItemRequestDTO> items
) {
}
