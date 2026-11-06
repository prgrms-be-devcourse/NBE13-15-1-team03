package com.kdt.cafe.entity.order;

import com.kdt.cafe.entity.Category;
import com.kdt.cafe.entity.Menu;
import com.kdt.cafe.exception.BusinessLogicException;
import com.kdt.cafe.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/*
 * 시각 경계값 업무 규칙 - Order 도메인 테스트 (배송일 계산 · 취소 마감 · 재취소)
 * - 실제 PC·서버 시계를 바꾸지 않고, 시각을 매개변수로 받는 메서드에 경계 시각을 직접 넣어 확인합니다.
 * - 모의 객체 없이 실제 Order 엔티티의 업무 로직을 실행합니다.
 */
class OrderTest {

    private static final LocalDate SHIPPING_DATE = LocalDate.of(2026, 11, 3);

    // ===== 배송일 계산: 14:00:00 전 접수 → 당일, 14:00:00부터 → 다음 날 =====

    @Test
    @DisplayName("배송일: 13:59:59 접수는 당일 배송이다")
    void shipping_date_before_cutoff_is_same_day() {
        assertEquals(LocalDate.of(2026, 11, 3),
                Order.calculateShippingDate(LocalDateTime.of(2026, 11, 3, 13, 59, 59)));
    }

    @Test
    @DisplayName("배송일: 14:00:00 정각 접수는 다음 날 배송이다")
    void shipping_date_at_cutoff_is_next_day() {
        assertEquals(LocalDate.of(2026, 11, 4),
                Order.calculateShippingDate(LocalDateTime.of(2026, 11, 3, 14, 0, 0)));
    }

    @Test
    @DisplayName("배송일: 14:00:01 접수는 다음 날 배송이다")
    void shipping_date_after_cutoff_is_next_day() {
        assertEquals(LocalDate.of(2026, 11, 4),
                Order.calculateShippingDate(LocalDateTime.of(2026, 11, 3, 14, 0, 1)));
    }

    @Test
    @DisplayName("배송일: 12월 31일 14:00:00 접수는 다음 해 1월 1일 배송이다")
    void shipping_date_rolls_over_to_next_year() {
        assertEquals(LocalDate.of(2027, 1, 1),
                Order.calculateShippingDate(LocalDateTime.of(2026, 12, 31, 14, 0, 0)));
    }

    // ===== 취소 마감: 배송일 14:00:00 전까지만 취소 가능 =====

    @Test
    @DisplayName("취소: 배송일 13:59:59에는 취소에 성공하고 status·cancelledAt만 바뀐다")
    void cancel_before_deadline_succeeds() {
        Order order = order();
        LocalDateTime now = SHIPPING_DATE.atTime(13, 59, 59);

        assertDoesNotThrow(() -> order.cancel(now));

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals(now, order.getCancelledAt());
        assertEquals(17000, order.getTotalAmount());
    }

    @Test
    @DisplayName("취소: 배송일 14:00:00 정각부터는 ORDER_NOT_CANCELLABLE이고 주문은 바뀌지 않는다")
    void cancel_at_deadline_is_rejected() {
        Order order = order();

        BusinessLogicException e = assertThrows(BusinessLogicException.class,
                () -> order.cancel(SHIPPING_DATE.atTime(14, 0, 0)));

        assertEquals(ErrorCode.ORDER_NOT_CANCELLABLE, e.getCode());
        assertEquals(OrderStatus.PLACED, order.getStatus());
        assertEquals(null, order.getCancelledAt());
    }

    // ===== 재취소: 이미 취소한 주문은 마감과 관계없이 성공, 최초 값 유지 =====

    @Test
    @DisplayName("재취소: 마감 이후에 다시 취소해도 예외 없이 성공하고, 최초 취소 시각과 기존 주문 값이 유지된다")
    void cancel_again_after_deadline_keeps_first_cancelled_at() {
        Order order = order();
        LocalDateTime firstCancelledAt = SHIPPING_DATE.atTime(10, 0, 0);
        order.cancel(firstCancelledAt);

        assertDoesNotThrow(() -> order.cancel(SHIPPING_DATE.atTime(15, 0, 0)));

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals(firstCancelledAt, order.getCancelledAt());
        assertEquals(17000, order.getTotalAmount());
        assertEquals(3, order.getTotalQuantity());
    }

    // ===== 도우미 =====

    // 2026-11-03 10:00 접수 → 배송일 2026-11-03 / 5000원×2 + 7000원×1 = 17,000원
    private static Order order() {
        Menu columbia = new Menu(new Category("싱글오리진"), "Columbia Nariñó", 5000, "");
        Menu brazil = new Menu(new Category("싱글오리진"), "Brazil Serra Do Caparaó", 7000, "");
        Order order = new Order("buyer@example.com", "서울시 실습로 10 101호", "01234",
                SHIPPING_DATE.atTime(10, 0), SHIPPING_DATE);
        order.addOrderItems(new OrderItem(columbia, 2, columbia.getName(), columbia.getPrice()));
        order.addOrderItems(new OrderItem(brazil, 1, brazil.getName(), brazil.getPrice()));
        return order;
    }
}
