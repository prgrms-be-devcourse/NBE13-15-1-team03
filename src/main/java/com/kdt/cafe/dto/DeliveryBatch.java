package com.kdt.cafe.dto;

import com.kdt.cafe.entity.Order;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
