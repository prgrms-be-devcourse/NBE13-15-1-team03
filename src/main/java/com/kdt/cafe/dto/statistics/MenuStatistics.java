package com.kdt.cafe.dto.statistics;

import com.kdt.cafe.entity.order.OrderItemAggregation;

public record MenuStatistics(
        int menuId,
        String menuName,
        int unitPrice,
        int totalQuantity,
        long totalAmount // 하루 합계는 int 범위를 넘을 수 있음
) {

    // 합산 키(메뉴 ID·주문 당시 메뉴명·단가) + 합산 수량 → 메뉴 통계 한 줄
    public static MenuStatistics from(OrderItemAggregation.Key key, int quantity) {
        return new MenuStatistics(key.menuId(), key.menuName(), key.unitPrice(),
                quantity, key.lineAmount(quantity));
    }
}
