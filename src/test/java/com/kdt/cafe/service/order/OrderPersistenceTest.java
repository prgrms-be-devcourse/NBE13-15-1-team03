package com.kdt.cafe.service.order;

import com.kdt.cafe.dto.order.OrderCreateRequestDTO;
import com.kdt.cafe.dto.order.OrderItemRequestDTO;
import com.kdt.cafe.dto.order.OrderItemResponseDTO;
import com.kdt.cafe.dto.order.OrderListRequestDTO;
import com.kdt.cafe.dto.order.OrderResponseDTO;
import com.kdt.cafe.entity.Menu;
import com.kdt.cafe.entity.order.Order;
import com.kdt.cafe.entity.order.OrderStatus;
import com.kdt.cafe.repository.MenuRepository;
import com.kdt.cafe.repository.OrderItemRepository;
import com.kdt.cafe.repository.OrderRepository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;

/*
 * 실제 DB 저장·롤백 테스트 (H2 인메모리, src/test/resources/application.yaml)
 * - 테스트에 @Transactional을 붙이지 않음: 서비스 호출마다 트랜잭션이 따로 커밋·롤백되고, 조회도 DB를 다시 읽음
 */
@SpringBootTest
class OrderPersistenceTest {

    private static final String EMAIL = "persist@example.com";

    @Autowired OrderService orderService;
    @Autowired MenuRepository menuRepository;
    @Autowired OrderItemRepository orderItemRepository;
    @Autowired EntityManager entityManager;
    @MockitoSpyBean OrderRepository orderRepository;

    private Menu columbia;
    private Menu brazil;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        columbia = findMenu("Columbia Nariñó");
        brazil = findMenu("Brazil Serra Do Caparaó");
    }

    @AfterEach
    void tearDown() {
        reset(orderRepository);
        orderRepository.deleteAll();
    }

    @Test
    @DisplayName("정상 주문 후 DB에서 다시 조회한 주문에 접수 당시 메뉴명·단가·수량·항목 금액·총액이 저장되어 있다")
    void saved_order_matches_request() {
        Long orderId = orderService.createOrder(request()).id();

        OrderResponseDTO saved = orderService.getOrder(orderId);

        assertEquals(EMAIL, saved.email());
        assertEquals(OrderStatus.PLACED, saved.status());
        assertEquals("", saved.memo());
        assertEquals(2, saved.items().size());
        assertItem(itemOf(saved, columbia), columbia, 2, 10000);
        assertItem(itemOf(saved, brazil), brazil, 1, 7000);
        assertEquals(17000, saved.totalAmount());
    }

    @Test
    @DisplayName("주문 행과 항목을 INSERT한 뒤 실패하면 주문·항목이 하나도 남지 않고, 이후 정상 주문은 저장된다")
    void failure_after_insert_rolls_back_everything() {
        long ordersBefore = orderRepository.count();
        long itemsBefore = orderItemRepository.count();
        Order[] inserted = new Order[1];

        // save 호출을 가로채 주문·항목을 실제로 INSERT한 뒤 예외를 던짐
        doAnswer(invocation -> {
            inserted[0] = invocation.getArgument(0);
            entityManager.persist(inserted[0]);
            throw new IllegalStateException("테스트용 실패: 주문·항목 INSERT 직후");
        }).when(orderRepository).save(any());

        assertThrows(IllegalStateException.class, () -> orderService.createOrder(request()));

        // ID가 발급됐다 = INSERT가 실행됐다 (입력 검사 단계에서 거절된 것이 아님)
        assertNotNull(inserted[0].getId());
        assertNotNull(inserted[0].getOrderItems().getFirst().getId());

        assertEquals(ordersBefore, orderRepository.count());
        assertEquals(itemsBefore, orderItemRepository.count());
        assertTrue(orderService.getOrdersByEmail(new OrderListRequestDTO(EMAIL)).isEmpty());

        reset(orderRepository);
        Long orderId = orderService.createOrder(request()).id();

        assertEquals(ordersBefore + 1, orderRepository.count());
        assertEquals(itemsBefore + 2, orderItemRepository.count());
        assertEquals(2, orderService.getOrder(orderId).items().size());
    }

    private OrderCreateRequestDTO request() {
        return new OrderCreateRequestDTO(EMAIL, "서울시 실습로 10 101호", "01234", List.of(
                new OrderItemRequestDTO(columbia.getId(), 2),
                new OrderItemRequestDTO(brazil.getId(), 1)));
    }

    private OrderItemResponseDTO itemOf(OrderResponseDTO order, Menu menu) {
        return order.items().stream()
                .filter(item -> item.menuId() == menu.getId())
                .findFirst()
                .orElseThrow();
    }

    private void assertItem(OrderItemResponseDTO item, Menu menu, int quantity, int lineAmount) {
        assertEquals(menu.getName(), item.menuName());
        assertEquals(menu.getPrice(), item.unitPrice());
        assertEquals(quantity, item.quantity());
        assertEquals(lineAmount, item.lineAmount());
    }

    private Menu findMenu(String name) {
        return menuRepository.findAll().stream()
                .filter(menu -> menu.getName().equals(name))
                .findFirst()
                .orElseThrow();
    }
}
