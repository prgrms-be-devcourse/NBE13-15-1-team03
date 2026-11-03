package com.kdt.cafe.dto;

import com.kdt.cafe.entity.Order;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

public record OrderResponseDTO(Long id,
                               String email,
                               String address,
                               String postcode,
                               OffsetDateTime createdAt,
                               LocalDate shippingDate,
                               int totalAmount,
                               List<OrderItemResponseDTO> items,
                               String status,
                               OffsetDateTime cancelledAt,
                               String memo) {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    public static OrderResponseDTO from(Order order){
        // 항목 엔티티 리스트 → 항목 DTO 리스트
        List<OrderItemResponseDTO> items = order.getOrderItems().stream()
                .map(OrderItemResponseDTO::from) //스트림으로 리스트의 각 항목을 꺼내 OrderItemResponseDTO::from에 넣고 결과를 새 리스트로 모은 것.
                .toList();

        // 각 항목 금액(lineAmount)의 합계
        int totalAmount = items.stream()
                .mapToInt(OrderItemResponseDTO::lineAmount)
                .sum();

        return new OrderResponseDTO(
                order.getId(),
                order.getEmail(),
                order.getAddress(),
                order.getPostcode(),
                order.getCreatedAt().atZone(SEOUL).toOffsetDateTime(), // +09:00 붙이기
                order.getShippingDate(),
                totalAmount,
                items,
                order.getStatus(),
                null,
                order.getMemo() == null ? "" : order.getMemo()
        );
    }

}
