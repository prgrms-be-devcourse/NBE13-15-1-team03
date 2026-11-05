package com.kdt.cafe.entity;

import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * - 주문 목록 묶음
 * - 메뉴 ID·주문 당시 단가·주문 당시 메뉴명이 모두 같은 항목끼리 수량 합산
 * - 메뉴 ID → 단가 → 메뉴명 오름차순 정렬
 * - 전체 수량·금액 계산
 * - 메뉴 아이디, 메뉴 단가, 메뉴명 - 전체 수량, 전체 금액 레코드 생성
 */
public record OrderItemAggregation(
        SortedMap<Key, Integer> quantityByKey,
        int totalQuantity,
        long totalAmount // 여러 주문 합계는 int 범위를 넘을 수 있음
) {

    // 합산 기준 키 (주문 당시 스냅샷 값), 정렬 기준 포함
    public record Key(int menuId, int unitPrice, String menuName) {

        private static final Comparator<Key> ORDER = Comparator.comparingInt(Key::menuId)
                .thenComparingInt(Key::unitPrice)
                .thenComparing(Key::menuName);

        public long lineAmount(int quantity) {
            return (long) unitPrice * quantity;
        }
    }

    public static OrderItemAggregation of(List<Order> orders) {
        SortedMap<Key, Integer> quantityByKey = new TreeMap<>(Key.ORDER);
        int totalQuantity = 0;
        long totalAmount = 0;

        for (Order order : orders) {
            for (OrderItem item : order.getOrderItems()) {
                Key key = new Key(item.getMenu().getId(), item.getUnitPrice(), item.getMenuName());
                quantityByKey.merge(key, item.getQuantity(), Integer::sum);
                totalQuantity += item.getQuantity();
                totalAmount += item.getLineAmount();
            }
        }
        return new OrderItemAggregation(quantityByKey, totalQuantity, totalAmount);
    }
}
