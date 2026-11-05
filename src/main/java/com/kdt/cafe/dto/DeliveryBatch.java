package com.kdt.cafe.dto;

import java.time.LocalDate;
import java.util.List;

public record DeliveryBatch(
        LocalDate shippingDate,
        String email,
        String address,
        String postcode,
        List<Long> orderIds,
        int orderCount,
        int totalQuantity,
        int totalAmount,
        List<OrderItemResponseDTO> items
) {
}
