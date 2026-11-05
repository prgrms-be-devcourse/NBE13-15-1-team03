package com.kdt.cafe.dto;

import java.time.LocalDate;

public record OrderStatistics(
        LocalDate date,
        int orderCount,
        int placedOrderCount,
        int cancelledOrderCount,
        int totalQuantity,
        int totalAmount
) {
}
