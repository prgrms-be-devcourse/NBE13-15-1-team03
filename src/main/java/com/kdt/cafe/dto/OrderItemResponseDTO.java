package com.kdt.cafe.dto;

import com.kdt.cafe.entity.OrderItem;

public record OrderItemResponseDTO(Long menuId, String menuName, int unitPrice, int quantity, int lineAmount) {

    public static OrderItemResponseDTO from(OrderItem orderItem){
        return new OrderItemResponseDTO((long) orderItem.getMenu().getId(), orderItem.getMenuName(), orderItem.getUnitPrice(), orderItem.getQuantity(), orderItem.getUnitPrice() * orderItem.getQuantity());
    }

}
