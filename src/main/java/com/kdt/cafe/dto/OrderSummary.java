package com.kdt.cafe.dto;

import com.kdt.cafe.entity.Order;
import com.kdt.cafe.entity.OrderStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record OrderSummary(Long id, LocalDateTime createdAt, LocalDate shippingDate, int totalAmount,
                           OrderStatus status) {

    public static OrderSummary from(Order order) {
        return new OrderSummary(order.getId(), order.getCreatedAt(), order.getShippingDate(), order.getTotalAmount(), order.getStatus());
    }
}
