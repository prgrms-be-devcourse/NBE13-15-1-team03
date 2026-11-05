package com.kdt.cafe.dto.delivery;

import com.kdt.cafe.dto.order.OrderItemResponseDTO;

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
        long totalAmount, // 묶음 합계는 int 범위를 넘을 수 있음
        List<OrderItemResponseDTO> items
) {
}
