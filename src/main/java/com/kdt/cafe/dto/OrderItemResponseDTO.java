package com.kdt.cafe.dto;

import com.kdt.cafe.entity.OrderItem;
import com.kdt.cafe.entity.OrderItemAggregation;

// lineAmount: 배송 묶음에서 합산된 항목은 int 범위를 넘을 수 있어 long
public record OrderItemResponseDTO(int menuId, String menuName, int unitPrice, int quantity, long lineAmount) {

    public static OrderItemResponseDTO from(OrderItem orderItem){
        return new OrderItemResponseDTO(orderItem.getMenu().getId(), orderItem.getMenuName(), orderItem.getUnitPrice(), orderItem.getQuantity(), orderItem.getLineAmount());
    }

    // 합산 키(메뉴 ID·주문 당시 메뉴명·단가) + 합산 수량 → 배송 묶음 항목
    public static OrderItemResponseDTO from(OrderItemAggregation.Key key, int quantity){
        return new OrderItemResponseDTO(key.menuId(), key.menuName(), key.unitPrice(), quantity, key.lineAmount(quantity));
    }
}
