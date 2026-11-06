package com.kdt.cafe.service.order;

import com.kdt.cafe.entity.Category;
import com.kdt.cafe.entity.Menu;
import com.kdt.cafe.entity.order.Order;
import com.kdt.cafe.entity.order.OrderItem;
import com.kdt.cafe.entity.order.OrderStatus;
import com.kdt.cafe.exception.BusinessLogicException;
import com.kdt.cafe.exception.ErrorCode;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class AdditionalTest {

    //배송일 계산 테스트
    @Test
    @DisplayName("13:59:59 접수는 당일 배송")
    void orderToday() {
        LocalDateTime orderAt = LocalDateTime.of(2026, 11, 6, 13, 59, 59);

        LocalDate shippingDate = Order.calculateShippingDate(orderAt);

        assertEquals(LocalDate.of(2026, 11, 6), shippingDate);
    }

    @Test
    @DisplayName("14:00:00 접수는 다음 날 배송")
    void orderNext() {
        LocalDateTime orderAt = LocalDateTime.of(2026, 11, 6, 14, 0, 0);

        LocalDate shippingDate = Order.calculateShippingDate(orderAt);

        assertEquals(LocalDate.of(2026, 11, 7), shippingDate);
    }

    @Test
    @DisplayName("14:00:01 접수는 다음 날 배송")
    void orderLate() {
        LocalDateTime orderAt = LocalDateTime.of(2026, 11, 6, 14, 0, 1);

        LocalDate shippingDate = Order.calculateShippingDate(orderAt);

        assertEquals(LocalDate.of(2026, 11, 7), shippingDate);
    }

    @Test
    @DisplayName("12월 31일 14:00:00 접수는 다음 해 1월 1일 배송")
    void orderFinal() {
        LocalDateTime orderAt = LocalDateTime.of(2025, 12, 31, 14, 0, 0);

        LocalDate shippingDate = Order.calculateShippingDate(orderAt);

        assertEquals(LocalDate.of(2026, 1, 1), shippingDate);
    }

    //시간에 따른 취소 테스트
    //사전 설정
    // 배송일 전날 10:00에 접수된 주문
    private static Order order(LocalDate shippingDate) {
        return new Order("buyer@example.com", "서울시 실습로 10 101호", "01234",
                shippingDate.minusDays(1).atTime(10, 0), shippingDate);
    }

    @Test
    @DisplayName("당일 14시 전에는 취소가 가능하다")
    void canCancel() {
        Order order = order(LocalDate.of(2026, 11, 7));

        order.cancel(LocalDateTime.of(2026, 11, 7, 13, 59, 59));

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    @DisplayName("당일 14시 부터는 취소가 불가능하다")
    void notCancel() {
        Order order = order(LocalDate.of(2026, 11, 7));

        BusinessLogicException e = assertThrows(BusinessLogicException.class,
                () -> order.cancel(LocalDateTime.of(2026, 11, 7, 14, 0, 0)));

        assertEquals(ErrorCode.ORDER_NOT_CANCELLABLE, e.getCode());
        assertEquals(OrderStatus.PLACED, order.getStatus());
    }

    //재취소 정상응답. (시간 이전에 취소후, 시간 이후에 다시 취소)
    @Test
    @DisplayName("재취소는 응답이 잘 온다")
    void againCancel() {
        Order order = order(LocalDate.of(2026, 11, 7));
        LocalDateTime firstCancel = LocalDateTime.of(2026, 11, 7, 13, 59, 59);

        order.cancel(firstCancel);
        assertDoesNotThrow(() -> order.cancel(LocalDateTime.of(2026, 11, 7, 15, 0, 0)));

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals(firstCancel, order.getCancelledAt());
    }

    //재주문 잘 되는지
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

    @Test
    @DisplayName("재주문은 가격만 업데이트하고 동일하게 주문을 넣는다.")
    void againOrder(){
        Long reId = 3L;

        //기존주문: Columbia 5000원 × 2개
        LocalDate shippingDate = LocalDate.of(2099, 12, 1);
        Order original = new Order(BUYER, ADDRESS, POSTCODE, shippingDate.atTime(9, 0), shippingDate);
        ReflectionTestUtils.setField(original, "id", reId);
        original.addOrderItems(new OrderItem(this.columbia, 2, this.columbia.getName(), 5000));
        given(this.orderRepository.findById(reId)).willReturn(Optional.of(original));

        //현재의 메뉴 가격 변경 5000 -> 6000
        this.columbia.update(this.columbia.getCategory(), this.columbia.getName(), 6000, this.columbia.getDescription());

        this.orderService.reOrder(reId);

        //재주문 되는지 확인(가격 반영)
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(this.orderRepository).save(captor.capture());
        assertEquals(6000, captor.getValue().getOrderItems().getFirst().getUnitPrice());

        //가격 외에는 원본과 같은지
        assertEquals(5000, original.getOrderItems().getFirst().getUnitPrice());
    }

}
