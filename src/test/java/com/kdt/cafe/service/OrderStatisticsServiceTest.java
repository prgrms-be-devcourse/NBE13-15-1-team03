package com.kdt.cafe.service;

import com.kdt.cafe.dto.CustomerStatistics;
import com.kdt.cafe.dto.MenuStatistics;
import com.kdt.cafe.dto.OrderStatistics;
import com.kdt.cafe.dto.StatisticsRequestDTO;
import com.kdt.cafe.entity.Category;
import com.kdt.cafe.entity.Menu;
import com.kdt.cafe.entity.Order;
import com.kdt.cafe.entity.OrderItem;
import com.kdt.cafe.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/*
 * A17 주문 통계 · A18 메뉴 통계 · A24 고객 통계 - OrderStatisticsService 테스트
 */
class OrderStatisticsServiceTest {

    private static final LocalDate DATE = LocalDate.of(2026, 11, 3);

    private OrderRepository orderRepository;
    private OrderStatisticsService orderStatisticsService;

    private Menu columbia;
    private Menu brazil;

    @BeforeEach
    void setUp() {
        this.orderRepository = mock(OrderRepository.class);
        this.orderStatisticsService = new OrderStatisticsService(this.orderRepository);

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
        assertFalse(end.getValue().isAfter(DATE.plusDays(1).atStartOfDay())); // 다음 날 00:00:00 제외
    }

    // ===== A18 접수일별 메뉴 통계 =====

    @Test
    @DisplayName("A18 같은 메뉴·주문 당시 단가·메뉴명 항목은 합산하고 메뉴 ID 오름차순으로 반환한다 (명세 예시)")
    void menu_statistics_merge_same_items() {
        givenOrders(
                order(101L, item(this.brazil, 1), item(this.columbia, 2)),
                order(102L, item(this.columbia, 1)));

        assertEquals(List.of(
                new MenuStatistics(1, "Columbia Nariñó", 5000, 3, 15000),
                new MenuStatistics(2, "Brazil Serra Do Caparaó", 7000, 1, 7000)
        ), menuStatistics());
    }

    @Test
    @DisplayName("A18 취소 주문의 항목은 메뉴 통계에 포함하지 않는다 (명세 예시: 101 취소)")
    void menu_statistics_exclude_cancelled_orders() {
        Order order101 = order(101L, item(this.columbia, 2), item(this.brazil, 1));
        Order order102 = order(102L, item(this.columbia, 1));
        order101.cancel(DATE.atTime(13, 30));
        givenOrders(order101, order102);

        assertEquals(List.of(new MenuStatistics(1, "Columbia Nariñó", 5000, 1, 5000)), menuStatistics());
    }

    @Test
    @DisplayName("A18 메뉴 ID가 같아도 주문 당시 단가·메뉴명이 다르면 따로 집계하고, 메뉴 ID → 단가 → 메뉴명 오름차순이다")
    void menu_statistics_split_by_snapshot() {
        // 같은 메뉴 1이지만 주문 사이에 가격·이름이 바뀐 상황
        givenOrders(
                order(201L, snapshotItem(this.columbia, 1, "Columbia Nariñó", 6000)),
                order(202L, snapshotItem(this.columbia, 2, "Columbia Nariñó", 5000)),
                order(203L, snapshotItem(this.columbia, 1, "Columbia Nariñó NEW", 5000)));

        assertEquals(List.of(
                new MenuStatistics(1, "Columbia Nariñó", 5000, 2, 10000),
                new MenuStatistics(1, "Columbia Nariñó NEW", 5000, 1, 5000),
                new MenuStatistics(1, "Columbia Nariñó", 6000, 1, 6000)
        ), menuStatistics());
    }

    @Test
    @DisplayName("A18 주문이 없거나 모두 취소된 날짜의 메뉴 통계는 빈 목록이다")
    void menu_statistics_empty_when_no_placed_orders() {
        givenOrders();
        assertEquals(List.of(), menuStatistics());

        Order order101 = order(101L, item(this.columbia, 2));
        order101.cancel(DATE.atTime(13, 30));
        givenOrders(order101);
        assertEquals(List.of(), menuStatistics());
    }

    // ===== A24 접수일별 고객 통계 =====

    @Test
    @DisplayName("A24 같은 이메일 주문은 한 줄로 집계하고, 값은 주문 통계와 같은 규칙이다 (명세 예시)")
    void customer_statistics_group_by_email() {
        givenOrders(
                order(101L, BUYER, item(this.columbia, 2), item(this.brazil, 1)),
                order(102L, BUYER, item(this.columbia, 1)));

        assertEquals(List.of(new CustomerStatistics(DATE, BUYER, 2, 2, 0, 4, 22000)), customerStatistics());
    }

    @Test
    @DisplayName("A24 취소 주문은 전체·CANCELLED 건수에만 포함하고 수량·금액에서는 제외한다 (명세 예시: 101 취소)")
    void customer_statistics_exclude_cancelled_amounts() {
        Order order101 = order(101L, BUYER, item(this.columbia, 2), item(this.brazil, 1));
        Order order102 = order(102L, BUYER, item(this.columbia, 1));
        order101.cancel(DATE.atTime(13, 30));
        givenOrders(order101, order102);

        assertEquals(List.of(new CustomerStatistics(DATE, BUYER, 2, 1, 1, 1, 5000)), customerStatistics());
    }

    @Test
    @DisplayName("A24 주문을 모두 취소한 고객도 포함하고, PLACED 건수·수량·금액은 0이다")
    void customer_statistics_include_all_cancelled_customer() {
        Order cancelled = order(201L, "alice@example.com", item(this.columbia, 5));
        cancelled.cancel(DATE.atTime(13, 30));
        givenOrders(cancelled, order(202L, BUYER, item(this.columbia, 1)));

        assertEquals(List.of(
                new CustomerStatistics(DATE, "alice@example.com", 1, 0, 1, 0, 0),
                new CustomerStatistics(DATE, BUYER, 1, 1, 0, 1, 5000)
        ), customerStatistics());
    }

    @Test
    @DisplayName("A24 이메일은 대소문자를 구분해 다른 고객이고, 결과는 이메일 오름차순이다")
    void customer_statistics_sorted_by_email_case_sensitive() {
        givenOrders(
                order(301L, "carol@example.com", item(this.columbia, 1)),
                order(302L, BUYER, item(this.columbia, 1)),
                order(303L, "Buyer@example.com", item(this.columbia, 1)),
                order(304L, "alice@example.com", item(this.columbia, 1)));

        // 문자 코드 순서: 대문자(B=66)가 소문자(a=97)보다 앞
        List<String> emails = customerStatistics().stream().map(CustomerStatistics::email).toList();
        assertEquals(List.of("Buyer@example.com", "alice@example.com", BUYER, "carol@example.com"), emails);
    }

    @Test
    @DisplayName("A24 주문이 없는 날짜의 고객 통계는 빈 목록이다")
    void customer_statistics_empty_when_no_orders() {
        givenOrders();

        assertEquals(List.of(), customerStatistics());
    }

    // ===== 도우미 =====

    private static final String BUYER = "buyer@example.com";

    private List<CustomerStatistics> customerStatistics() {
        return this.orderStatisticsService.customerAnalytics(new StatisticsRequestDTO(DATE));
    }

    private List<MenuStatistics> menuStatistics() {
        return this.orderStatisticsService.menuAnalytics(new StatisticsRequestDTO(DATE));
    }

    private OrderStatistics statistics() {
        return this.orderStatisticsService.orderAnalytics(new StatisticsRequestDTO(DATE));
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

    // 주문 당시 이름·단가가 현재 메뉴와 다른 항목
    private static OrderItem snapshotItem(Menu menu, int quantity, String menuName, int unitPrice) {
        return new OrderItem(menu, quantity, menuName, unitPrice);
    }

    // 접수일 10:00 접수 → 배송일은 같은 날
    private static Order order(Long id, OrderItem... items) {
        return order(id, BUYER, items);
    }

    private static Order order(Long id, String email, OrderItem... items) {
        Order order = new Order(email, "서울시 실습로 10 101호", "01234", DATE.atTime(10, 0), DATE);
        ReflectionTestUtils.setField(order, "id", id);
        for (OrderItem item : items) {
            order.addOrderItems(item);
        }
        return order;
    }
}
