package com.kdt.cafe.service;

import com.kdt.cafe.dto.OrderStatistics;
import com.kdt.cafe.dto.OrderStatisticsRequestDTO;
import com.kdt.cafe.entity.Category;
import com.kdt.cafe.entity.Menu;
import com.kdt.cafe.entity.Order;
import com.kdt.cafe.entity.OrderItem;
import com.kdt.cafe.repository.MenuRepository;
import com.kdt.cafe.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/*
 * A17 접수일별 주문 통계 - OrderService.statistics 업무 규칙 테스트
 * - Repository만 모의 객체로 두고, 실제 Order/OrderItem 엔티티로 집계 로직을 실행합니다.
 */
class OrderServiceStatisticsTest {

    private static final LocalDate DATE = LocalDate.of(2026, 11, 3);

    private OrderRepository orderRepository;
    private OrderService orderService;

    private Menu columbia;
    private Menu brazil;

    @BeforeEach
    void setUp() {
        this.orderRepository = mock(OrderRepository.class);
        this.orderService = new OrderService(this.orderRepository, mock(MenuRepository.class));

        this.columbia = menu(1, "Columbia Nariñó", 5000);
        this.brazil = menu(2, "Brazil Serra Do Caparaó", 7000);
    }

    @Test
    @DisplayName("A17 PLACED 주문 2건이면 전체·PLACED 2건, CANCELLED 0건, 수량·금액은 두 주문의 합계다 (명세 예시)")
    void placed_orders_are_summed() {
        Order order101 = order(101L, item(this.columbia, 2), item(this.brazil, 1)); // 3개, 17,000원
        Order order102 = order(102L, item(this.columbia, 1));                       // 1개, 5,000원
        givenOrders(order101, order102);

        OrderStatistics result = statistics();

        assertEquals(new OrderStatistics(DATE, 2, 2, 0, 4, 22000), result);
    }

    @Test
    @DisplayName("A17 취소 주문은 전체·CANCELLED 건수에만 포함하고 수량·금액에서는 제외한다 (명세 예시: 101 취소)")
    void cancelled_orders_are_counted_but_not_summed() {
        Order order101 = order(101L, item(this.columbia, 2), item(this.brazil, 1));
        Order order102 = order(102L, item(this.columbia, 1));
        order101.cancel(DATE.atTime(13, 30));
        givenOrders(order101, order102);

        OrderStatistics result = statistics();

        assertEquals(new OrderStatistics(DATE, 2, 1, 1, 1, 5000), result);
    }

    @Test
    @DisplayName("A17 주문을 모두 취소한 날은 전체·CANCELLED 건수만 있고 PLACED 건수·수량·금액은 0이다")
    void all_cancelled_day_has_zero_amounts() {
        Order order101 = order(101L, item(this.columbia, 2));
        order101.cancel(DATE.atTime(13, 30));
        givenOrders(order101);

        OrderStatistics result = statistics();

        assertEquals(new OrderStatistics(DATE, 1, 0, 1, 0, 0), result);
    }

    @Test
    @DisplayName("A17 주문이 없는 날짜도 조회한 date와 모든 값이 0인 객체를 반환한다")
    void no_orders_returns_zero_statistics() {
        givenOrders();

        OrderStatistics result = statistics();

        assertEquals(new OrderStatistics(DATE, 0, 0, 0, 0, 0), result);
    }

    @Test
    @DisplayName("A17 조회 범위는 서울 기준 접수일 00:00:00부터 23:59:59까지를 포함하고 다음 날 00:00:00은 포함하지 않는다")
    void query_range_covers_whole_created_date() {
        givenOrders();

        statistics();

        // Repository 쿼리: createdAt >= start and createdAt < end
        ArgumentCaptor<LocalDateTime> start = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> end = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(this.orderRepository).findAllByCreatedDate(start.capture(), end.capture());

        assertEquals(DATE.atStartOfDay(), start.getValue());
        assertTrue(end.getValue().isAfter(DATE.atTime(23, 59, 59)));          // 23:59:59 접수 포함
        assertTrue(!end.getValue().isAfter(DATE.plusDays(1).atStartOfDay())); // 다음 날 00:00:00 제외
    }

    // ===== 도우미 =====

    private OrderStatistics statistics() {
        return this.orderService.statistics(new OrderStatisticsRequestDTO(DATE));
    }

    private void givenOrders(Order... orders) {
        given(this.orderRepository.findAllByCreatedDate(any(), any())).willReturn(List.of(orders));
    }

    private static Menu menu(int id, String name, int price) {
        Menu menu = new Menu(new Category("싱글오리진"), name, price, "");
        ReflectionTestUtils.setField(menu, "id", id);
        return menu;
    }

    private static OrderItem item(Menu menu, int quantity) {
        return new OrderItem(menu, quantity, menu.getName(), menu.getPrice());
    }

    // 접수일 10:00 접수 → 배송일은 같은 날
    private static Order order(Long id, OrderItem... items) {
        Order order = new Order("buyer@example.com", "서울시 실습로 10 101호", "01234", DATE.atTime(10, 0), DATE);
        ReflectionTestUtils.setField(order, "id", id);
        for (OrderItem item : items) {
            order.addOrderItems(item);
        }
        return order;
    }
}
