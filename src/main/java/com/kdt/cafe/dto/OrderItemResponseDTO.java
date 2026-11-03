package com.kdt.cafe.dto;

import com.kdt.cafe.entity.OrderItem;

public record OrderItemResponseDTO(int menuId, String menuName, int unitPrice, int quantity, int lineAmount) {

    public static OrderItemResponseDTO from(OrderItem orderItem){
        return new OrderItemResponseDTO(orderItem.getMenu().getId(), orderItem.getMenuName(), orderItem.getUnitPrice(), orderItem.getQuantity(), orderItem.getLineAmount());
    }
}
