package com.kdt.cafe.service;

import com.kdt.cafe.dto.OrderCreateRequestDTO;
import com.kdt.cafe.dto.OrderItemRequestDTO;
import com.kdt.cafe.dto.OrderListRequestDTO;
import com.kdt.cafe.dto.OrderSummary;
import com.kdt.cafe.entity.Category;
import com.kdt.cafe.entity.Menu;
import com.kdt.cafe.entity.Order;
import com.kdt.cafe.entity.OrderItem;
import com.kdt.cafe.entity.OrderStatus;
import com.kdt.cafe.exception.BusinessLogicException;
import com.kdt.cafe.exception.ErrorCode;
import com.kdt.cafe.repository.MenuRepository;
import com.kdt.cafe.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/*
 * A12 이메일별 주문 목록 · ADDRESS_MISMATCH(A11 주문 접수 · A23 재주문) - OrderService 업무 규칙 테스트
 * - Repository만 모의 객체로 두고, 실제 Order/OrderItem 엔티티와 OrderService 로직을 실행합니다.
 */
class OrderServiceTest {

    private static final String BUYER = "buyer@example.com";
    private static final String ADDRESS = "서울시 실습로 10 101호";
    private static final String POSTCODE = "01234";

    private OrderRepository orderRepository;
    private OrderService orderService;

    private Menu columbia;

    @BeforeEach
    void setUp() {
        this.orderRepository = mock(OrderRepository.class);
        MenuRepository menuRepository = mock(MenuRepository.class);
        this.orderService = new OrderService(this.orderRepository, menuRepository);

        this.columbia = new Menu(new Category("싱글오리진"), "Columbia Nariñó", 5000, "");
        ReflectionTestUtils.setField(this.columbia, "id", 1);
        given(menuRepository.findAllById(any())).willReturn(List.of(this.columbia));
        given(this.orderRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));
    }

    // ===== A12 이메일별 주문 목록 =====

    @Test
    @DisplayName("A12 접수 시각 내림차순·같으면 ID 내림차순으로 정렬하는 쿼리로 조회하고, 그 순서대로 OrderSummary를 반환한다")
    void orders_by_email_use_sorted_query() {
        Order newer = order(2L, BUYER, ADDRESS, POSTCODE);
        Order older = order(1L, BUYER, ADDRESS, POSTCODE);
        given(this.orderRepository.findByEmailOrderByCreatedAtDescIdDesc(BUYER)).willReturn(List.of(newer, older));

        List<OrderSummary> result = this.orderService.getOrdersByEmail(new OrderListRequestDTO(BUYER));

        assertEquals(List.of(2L, 1L), result.stream().map(OrderSummary::id).toList());
        verify(this.orderRepository).findByEmailOrderByCreatedAtDescIdDesc(BUYER);
    }

    @Test
    @DisplayName("A12 이메일은 대소문자를 구분하므로, DB가 함께 돌려준 대소문자만 다른 이메일의 주문은 제외한다")
    void orders_by_email_are_case_sensitive() {
        Order mine = order(2L, BUYER, ADDRESS, POSTCODE);
        Order otherCase = order(1L, "Buyer@example.com", ADDRESS, POSTCODE);
        given(this.orderRepository.findByEmailOrderByCreatedAtDescIdDesc(BUYER)).willReturn(List.of(mine, otherCase));

        List<OrderSummary> result = this.orderService.getOrdersByEmail(new OrderListRequestDTO(BUYER));

        assertEquals(List.of(2L), result.stream().map(OrderSummary::id).toList());
    }

    @Test
    @DisplayName("A12 취소 주문도 목록에 포함한다")
    void orders_by_email_include_cancelled() {
        Order cancelled = order(1L, BUYER, ADDRESS, POSTCODE);
        cancelled.cancel(cancelled.getShippingDate().atTime(9, 30));
        given(this.orderRepository.findByEmailOrderByCreatedAtDescIdDesc(BUYER)).willReturn(List.of(cancelled));

        List<OrderSummary> result = this.orderService.getOrdersByEmail(new OrderListRequestDTO(BUYER));

        assertEquals(OrderStatus.CANCELLED, result.getFirst().status());
    }

    // ===== ADDRESS_MISMATCH =====

    @Test
    @DisplayName("A11 같은 이메일·배송일의 PLACED 주문과 주소가 다르면 409 ADDRESS_MISMATCH이고 주문을 저장하지 않는다")
    void different_address_is_rejected() {
        givenPlacedOrders(order(1L, BUYER, ADDRESS, POSTCODE));

        BusinessLogicException e = assertThrows(BusinessLogicException.class,
                () -> this.orderService.createOrder(request(BUYER, "서울시 실습로 20 202호", POSTCODE)));

        assertEquals(ErrorCode.ADDRESS_MISMATCH, e.getCode());
        assertEquals(java.util.Map.of(), e.getFieldErrors());
        verify(this.orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("A11 주소가 같아도 우편번호가 다르면 409 ADDRESS_MISMATCH이다")
    void different_postcode_is_rejected() {
        givenPlacedOrders(order(1L, BUYER, ADDRESS, POSTCODE));

        BusinessLogicException e = assertThrows(BusinessLogicException.class,
                () -> this.orderService.createOrder(request(BUYER, ADDRESS, "09999")));

        assertEquals(ErrorCode.ADDRESS_MISMATCH, e.getCode());
        verify(this.orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("A11 주소·우편번호는 앞뒤 공백을 제거한 뒤 비교하므로 공백만 다르면 같은 배송지로 저장한다")
    void same_address_after_strip_is_accepted() {
        givenPlacedOrders(order(1L, BUYER, ADDRESS, POSTCODE));

        assertDoesNotThrow(() -> this.orderService.createOrder(request(BUYER, "  " + ADDRESS + "\t", " " + POSTCODE + " ")));
        verify(this.orderRepository).save(any());
    }

    @Test
    @DisplayName("A11 주소 중간 표기가 다르면(공백 위치) 다른 배송지로 보고 409 ADDRESS_MISMATCH이다")
    void different_inner_spacing_is_rejected() {
        givenPlacedOrders(order(1L, BUYER, ADDRESS, POSTCODE));

        BusinessLogicException e = assertThrows(BusinessLogicException.class,
                () -> this.orderService.createOrder(request(BUYER, "서울시 실습로 10 101 호", POSTCODE)));

        assertEquals(ErrorCode.ADDRESS_MISMATCH, e.getCode());
    }

    @Test
    @DisplayName("A11 같은 이메일·배송일에 PLACED 주문이 없으면(모두 취소 포함) 새 주소로 주문할 수 있고, PLACED 상태로만 조회한다")
    void no_placed_orders_allows_new_address() {
        givenPlacedOrders();

        assertDoesNotThrow(() -> this.orderService.createOrder(request(BUYER, "부산시 해운대로 1", "48000")));
        verify(this.orderRepository).findByEmailAndShippingDateAndStatus(eq(BUYER), any(LocalDate.class), eq(OrderStatus.PLACED));
        verify(this.orderRepository).save(any());
    }

    @Test
    @DisplayName("A11 이메일은 대소문자를 구분하므로, 대소문자만 다른 이메일의 주문 주소와는 비교하지 않는다")
    void other_case_email_is_not_compared() {
        givenPlacedOrders(order(1L, "Buyer@example.com", ADDRESS, POSTCODE));

        assertDoesNotThrow(() -> this.orderService.createOrder(request(BUYER, "부산시 해운대로 1", "48000")));
        verify(this.orderRepository).save(any());
    }

    @Test
    @DisplayName("A23 재주문에도 같은 배송지 조건을 적용해, 같은 배송일의 기존 묶음과 주소가 다르면 409 ADDRESS_MISMATCH이다")
    void reorder_applies_address_rule() {
        Order original = order(10L, BUYER, "서울시 옛주소 1", POSTCODE);
        given(this.orderRepository.findById(10L)).willReturn(Optional.of(original));
        givenPlacedOrders(order(11L, BUYER, ADDRESS, POSTCODE));

        BusinessLogicException e = assertThrows(BusinessLogicException.class, () -> this.orderService.reOrder(10L));

        assertEquals(ErrorCode.ADDRESS_MISMATCH, e.getCode());
        verify(this.orderRepository, never()).save(any());
    }

    // ===== 도우미 =====

    private void givenPlacedOrders(Order... orders) {
        given(this.orderRepository.findByEmailAndShippingDateAndStatus(any(), any(), any())).willReturn(List.of(orders));
    }

    private static OrderCreateRequestDTO request(String email, String address, String postcode) {
        return new OrderCreateRequestDTO(email, address, postcode, List.of(new OrderItemRequestDTO(1, 1)));
    }

    // 저장된 기존 주문 (배송일은 먼 미래로 두어 취소 마감에 걸리지 않게 함)
    private Order order(Long id, String email, String address, String postcode) {
        LocalDate shippingDate = LocalDate.of(2099, 12, 1);
        Order order = new Order(email, address, postcode, shippingDate.atTime(9, 0), shippingDate);
        ReflectionTestUtils.setField(order, "id", id);
        order.addOrderItems(new OrderItem(this.columbia, 1, this.columbia.getName(), this.columbia.getPrice()));
        return order;
    }
}
