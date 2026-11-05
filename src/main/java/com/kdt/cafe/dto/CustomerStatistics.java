package com.kdt.cafe.dto;

import java.time.LocalDate;

public record CustomerStatistics(
        LocalDate date,
        String email,
        int orderCount,
        int placedOrderCount,
        int cancelledOrderCount,
        int totalQuantity,
        long totalAmount
) {

    // 한 고객(이메일)의 주문으로 계산한 주문 통계 + 이메일 → 고객 통계 한 줄
    public static CustomerStatistics from(String email, OrderStatistics statistics) {
        return new CustomerStatistics(
                statistics.date(),
                email,
                statistics.orderCount(),
                statistics.placedOrderCount(),
                statistics.cancelledOrderCount(),
                statistics.totalQuantity(),
                statistics.totalAmount());
    }
}
