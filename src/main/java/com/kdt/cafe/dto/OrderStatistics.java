package com.kdt.cafe.dto;

import java.time.LocalDate;

public record OrderStatistics(
        LocalDate date,
        int orderCount,
        int placedOrderCount,
        int cancelledOrderCount,
        int totalQuantity,
        long totalAmount // 하루 합계는 int 범위를 넘을 수 있음
) {
}
