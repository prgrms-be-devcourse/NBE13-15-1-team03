package com.kdt.cafe.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 * 주문 항목 합산 규칙 - OrderItemAggregation 단위 테스트 (배송 묶음 A14 · 메뉴 통계 A18 공통)
 * - DB·Spring 없이 실제 Order/OrderItem 엔티티만으로 실행합니다.
 */
class OrderItemAggregationTest {

    private static final LocalDate DATE = LocalDate.of(2026, 11, 3);

    private final Menu columbia = menu(1, "Columbia Nariñó", 5000);
    private final Menu brazil = menu(2, "Brazil Serra Do Caparaó", 7000);

    @Test
    @DisplayName("여러 주문에서 메뉴 ID·단가·메뉴명이 같은 항목은 수량을 합산하고, 전체 수량·금액도 함께 계산한다")
    void merges_same_items_and_sums_totals() {
        Order order1 = order(item(this.columbia, 2, "Columbia Nariñó", 5000), item(this.brazil, 1, "Brazil Serra Do Caparaó", 7000));
        Order order2 = order(item(this.columbia, 1, "Columbia Nariñó", 5000));

        OrderItemAggregation result = OrderItemAggregation.of(List.of(order1, order2));

        assertEquals(List.of(
                new OrderItemAggregation.Key(1, 5000, "Columbia Nariñó"),
                new OrderItemAggregation.Key(2, 7000, "Brazil Serra Do Caparaó")
        ), List.copyOf(result.quantityByKey().keySet()));
        assertEquals(List.of(3, 1), List.copyOf(result.quantityByKey().values()));
        assertEquals(4, result.totalQuantity());
        assertEquals(22000, result.totalAmount());
    }

    @Test
    @DisplayName("단가나 메뉴명 중 하나라도 다르면 따로 집계하고, 키는 메뉴 ID → 단가 → 메뉴명 오름차순이다")
    void different_snapshot_is_separate_and_sorted() {
        Order order = order(
                item(this.brazil, 1, "Brazil Serra Do Caparaó", 7000),
                item(this.columbia, 1, "Columbia Nariñó", 6000),
                item(this.columbia, 1, "Columbia Nariñó NEW", 5000),
                item(this.columbia, 1, "Columbia Nariñó", 5000));

        OrderItemAggregation result = OrderItemAggregation.of(List.of(order));

        assertEquals(List.of(
                new OrderItemAggregation.Key(1, 5000, "Columbia Nariñó"),
                new OrderItemAggregation.Key(1, 5000, "Columbia Nariñó NEW"),
                new OrderItemAggregation.Key(1, 6000, "Columbia Nariñó"),
                new OrderItemAggregation.Key(2, 7000, "Brazil Serra Do Caparaó")
        ), List.copyOf(result.quantityByKey().keySet()));
        assertEquals(4, result.totalQuantity());
        assertEquals(23000, result.totalAmount());
    }

    @Test
    @DisplayName("주문이 없으면 합산 결과는 비어 있고 전체 수량·금액은 0이다")
    void empty_orders() {
        OrderItemAggregation result = OrderItemAggregation.of(List.of());

        assertTrue(result.quantityByKey().isEmpty());
        assertEquals(0, result.totalQuantity());
        assertEquals(0, result.totalAmount());
    }

    @Test
    @DisplayName("Key.lineAmount는 주문 당시 단가 × 합산 수량이다")
    void key_line_amount() {
        assertEquals(15000, new OrderItemAggregation.Key(1, 5000, "Columbia Nariñó").lineAmount(3));
    }

    // ===== 도우미 =====

    private static Menu menu(int id, String name, int price) {
        Menu menu = new Menu(new Category("싱글오리진"), name, price, "");
        ReflectionTestUtils.setField(menu, "id", id);
        return menu;
    }

    private static OrderItem item(Menu menu, int quantity, String menuName, int unitPrice) {
        return new OrderItem(menu, quantity, menuName, unitPrice);
    }

    private static Order order(OrderItem... items) {
        Order order = new Order("buyer@example.com", "서울시 실습로 10 101호", "01234", DATE.atTime(10, 0), DATE);
        for (OrderItem item : items) {
            order.addOrderItems(item);
        }
        return order;
    }
}
