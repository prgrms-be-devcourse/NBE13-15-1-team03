package com.kdt.cafe.service.order;

import com.kdt.cafe.dto.delivery.DeliveryBatch;
import com.kdt.cafe.dto.delivery.DeliveryRequestDto;
import com.kdt.cafe.dto.order.OrderItemResponseDTO;
import com.kdt.cafe.entity.Category;
import com.kdt.cafe.entity.Menu;
import com.kdt.cafe.entity.order.Order;
import com.kdt.cafe.entity.order.OrderItem;
import com.kdt.cafe.entity.order.OrderStatus;
import com.kdt.cafe.repository.OrderRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/*
 * A14 배송 묶음 - DeliveryService 업무 규칙 테스트
 * - Repository만 모의 객체로 두고, 실제 Order/OrderItem 엔티티로 묶음 계산 로직을 실행합니다.
 */
class DeliveryServiceTest {

    private static final LocalDate SHIPPING_DATE = LocalDate.of(2026, 11, 3);
    private static final String ADDRESS = "서울시 실습로 10 101호";
    private static final String POSTCODE = "01234";

    private OrderRepository orderRepository;
    private DeliveryService deliveryService;

    private Menu columbia;
    private Menu brazil;

    @BeforeEach
    void setUp() {
        this.orderRepository = mock(OrderRepository.class);
        this.deliveryService = new DeliveryService(this.orderRepository);

        this.columbia = menu(1, "Columbia Nariñó", 5000);
        this.brazil = menu(2, "Brazil Serra Do Caparaó", 7000);
    }

    @Test
    @DisplayName("A14 같은 이메일 주문은 한 묶음이 되고, 같은 메뉴 항목은 수량·금액을 합산한다")
    void same_email_orders_are_merged_into_one_batch() {
        Order order101 = order(101L, "buyer@example.com", item(this.columbia, 2), item(this.brazil, 1));
        Order order102 = order(102L, "buyer@example.com", item(this.columbia, 1));
        givenOrders(order101, order102);

        List<DeliveryBatch> result = getBatches();

        assertEquals(1, result.size());
        DeliveryBatch batch = result.getFirst();
        assertEquals(SHIPPING_DATE, batch.shippingDate());
        assertEquals("buyer@example.com", batch.email());
        assertEquals(ADDRESS, batch.address());
        assertEquals(POSTCODE, batch.postcode());
        assertEquals(List.of(101L, 102L), batch.orderIds());
        assertEquals(2, batch.orderCount());
        assertEquals(4, batch.totalQuantity());
        assertEquals(22000, batch.totalAmount());
        assertEquals(List.of(
                new OrderItemResponseDTO(1, "Columbia Nariñó", 5000, 3, 15000),
                new OrderItemResponseDTO(2, "Brazil Serra Do Caparaó", 7000, 1, 7000)
        ), batch.items());
    }

    @Test
    @DisplayName("A14 배송일의 PLACED 주문만 조회하도록 Repository에 PLACED 상태를 넘긴다")
    void queries_only_placed_orders() {
        givenOrders();

        getBatches();

        verify(this.orderRepository).findByShippingDateAndStatus(SHIPPING_DATE, OrderStatus.PLACED);
    }

    @Test
    @DisplayName("A14 해당 배송일에 주문이 없으면 빈 목록을 반환한다")
    void no_orders_returns_empty_list() {
        givenOrders();

        assertTrue(getBatches().isEmpty());
    }

    @Test
    @DisplayName("A14 이메일이 다르면 다른 묶음이고(대소문자 구분), 묶음은 이메일 오름차순이다")
    void batches_are_split_by_email_and_sorted() {
        givenOrders(
                order(201L, "carol@example.com", item(this.columbia, 1)),
                order(202L, "alice@example.com", item(this.columbia, 1)),
                order(203L, "buyer@example.com", item(this.columbia, 1)),
                order(204L, "Buyer@example.com", item(this.columbia, 1)));

        List<DeliveryBatch> result = getBatches();

        // 문자열 오름차순: 대문자(B)가 소문자(a)보다 앞
        List<String> emails = result.stream().map(DeliveryBatch::email).toList();
        assertEquals(List.of("Buyer@example.com", "alice@example.com", "buyer@example.com", "carol@example.com"), emails);
        result.forEach(batch -> assertEquals(1, batch.orderCount()));
    }

    @Test
    @DisplayName("A14 이메일이 섞여 있어도 각 묶음의 orderIds는 오름차순이고 orderCount는 그 길이와 같다")
    void order_ids_are_ascending_per_email() {
        // Repository는 쿼리의 order by o.id 대로 ID 오름차순으로 반환
        givenOrders(
                order(101L, "buyer@example.com", item(this.columbia, 1)),
                order(102L, "alice@example.com", item(this.columbia, 1)),
                order(103L, "buyer@example.com", item(this.brazil, 1)),
                order(105L, "buyer@example.com", item(this.columbia, 1)));

        List<DeliveryBatch> result = getBatches();

        assertEquals(List.of(102L), result.get(0).orderIds());
        assertEquals(List.of(101L, 103L, 105L), result.get(1).orderIds());
        assertEquals(3, result.get(1).orderCount());
    }

    @Test
    @DisplayName("A14 메뉴 ID가 같아도 주문 당시 단가·이름이 다르면 합산하지 않고, 메뉴 ID → 단가 → 메뉴명 오름차순이다")
    void items_with_different_snapshot_are_not_merged() {
        // 같은 메뉴 1이지만 주문 사이에 가격·이름이 바뀐 상황
        givenOrders(
                order(301L, "buyer@example.com",
                        item(this.brazil, 1),
                        snapshotItem(this.columbia, 1, "Columbia Nariñó", 6000)),
                order(302L, "buyer@example.com",
                        snapshotItem(this.columbia, 2, "Columbia Nariñó", 5000),
                        snapshotItem(this.columbia, 1, "Columbia Nariñó NEW", 5000)),
                order(303L, "buyer@example.com",
                        snapshotItem(this.columbia, 4, "Columbia Nariñó", 5000)));

        DeliveryBatch batch = getBatches().getFirst();

        assertEquals(List.of(
                new OrderItemResponseDTO(1, "Columbia Nariñó", 5000, 6, 30000),     // 2 + 4 합산
                new OrderItemResponseDTO(1, "Columbia Nariñó NEW", 5000, 1, 5000),  // 이름이 달라 별도
                new OrderItemResponseDTO(1, "Columbia Nariñó", 6000, 1, 6000),      // 단가가 달라 별도
                new OrderItemResponseDTO(2, "Brazil Serra Do Caparaó", 7000, 1, 7000)
        ), batch.items());
        assertEquals(9, batch.totalQuantity());
        assertEquals(48000, batch.totalAmount());
    }

    @Test
    @DisplayName("A14 항목별 합산 수량은 개별 주문 제한인 20개를 넘을 수 있다")
    void merged_quantity_can_exceed_20() {
        givenOrders(
                order(401L, "buyer@example.com", item(this.columbia, 20)),
                order(402L, "buyer@example.com", item(this.columbia, 15)));

        DeliveryBatch batch = getBatches().getFirst();

        assertEquals(List.of(new OrderItemResponseDTO(1, "Columbia Nariñó", 5000, 35, 175000)), batch.items());
        assertEquals(35, batch.totalQuantity());
        assertEquals(175000, batch.totalAmount());
    }

    // ===== 도우미 =====

    private List<DeliveryBatch> getBatches() {
        return this.deliveryService.getOrdersByShippingDate(new DeliveryRequestDto(SHIPPING_DATE));
    }

    private void givenOrders(Order... orders) {
        given(this.orderRepository.findByShippingDateAndStatus(SHIPPING_DATE, OrderStatus.PLACED))
                .willReturn(List.of(orders));
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

    private static Order order(Long id, String email, OrderItem... items) {
        Order order = new Order(email, ADDRESS, POSTCODE, LocalDateTime.of(2026, 11, 3, 10, 0), SHIPPING_DATE);
        ReflectionTestUtils.setField(order, "id", id);
        for (OrderItem item : items) {
            order.addOrderItems(item);
        }
        return order;
    }
}
