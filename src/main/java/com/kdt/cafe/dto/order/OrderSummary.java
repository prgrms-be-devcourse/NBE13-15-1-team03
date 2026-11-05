package com.kdt.cafe.dto.order;

import com.kdt.cafe.entity.order.Order;
import com.kdt.cafe.entity.order.OrderStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record OrderSummary(Long id, OffsetDateTime createdAt, LocalDate shippingDate, int totalAmount,
                           OrderStatus status) {


    public static OrderSummary from(Order order) {
        return new OrderSummary(
                order.getId(),
                order.getCreatedAt().atZone(Order.ZONE).toOffsetDateTime(), // +09:00 붙이기
                order.getShippingDate(),
                order.getTotalAmount(),
                order.getStatus());
    }
}
