package com.kdt.cafe.service.order;

import com.kdt.cafe.dto.order.*;
import com.kdt.cafe.entity.Menu;
import com.kdt.cafe.entity.order.Order;
import com.kdt.cafe.entity.order.OrderItem;
import com.kdt.cafe.entity.order.OrderStatus;
import com.kdt.cafe.exception.BusinessLogicException;
import com.kdt.cafe.exception.ErrorCode;
import com.kdt.cafe.repository.MenuRepository;
import com.kdt.cafe.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuRepository menuRepository;

    @Transactional
    public OrderResponseDTO createOrder(OrderCreateRequestDTO request) {
        return placeOrder(request.email(), request.address(), request.postcode(), request.items());
    }

    // 재주문에서 재사용하기 위해 작성한 메서드
    private OrderResponseDTO placeOrder(String email, String address, String postcode, List<OrderItemRequestDTO> items){

        // 1. 요청에서 메뉴 id 목록 뽑기
        List<Integer> menuIds = items.stream()
                .map(OrderItemRequestDTO::menuId)
                .toList();

        // 2. 메뉴들을 DB에서 한 번에 조회
        List<Menu> menus = this.menuRepository.findAllById(menuIds);

        // 3. 없는 메뉴가 있는지 검사
        if (menus.size() != menuIds.stream().distinct().count()){
            throw new BusinessLogicException(ErrorCode.MENU_NOT_FOUND);
        }

        // 4. 판매 중지 메뉴가 있는지 검사
        for (Menu menu : menus){
            if (!menu.isAvailable()){
                throw new BusinessLogicException(ErrorCode.MENU_UNAVAILABLE);
            }
        }

        // 5. 현재 시각 구하고, 배송일 계산
        LocalDateTime nowTime = now();
        LocalDate shippingDate = Order.calculateShippingDate(nowTime);

        // 5-1. 같은 이메일·배송일의 PLACED 주문과 배송지가 다르면 거절
        validateSameAddress(email, shippingDate, address, postcode);

        // 6. Order 만들고, 항목마다 OrderItem 만들어서 추가
        Order order = new Order(email, address, postcode, nowTime, shippingDate);

        Map<Integer, Menu> menuById = menus.stream()
                .collect(Collectors.toMap(Menu::getId,menu -> menu));

        for (OrderItemRequestDTO itemRequest : items){
            Menu menu = menuById.get(itemRequest.menuId());
            OrderItem orderItem = new OrderItem(menu, itemRequest.quantity(), menu.getName(), menu.getPrice());
            order.addOrderItems(orderItem);
        }

        // 7. 저장하고, 응답 DTO로 바꿔서 반환
        Order savedOrder = orderRepository.save(order);
        return OrderResponseDTO.from(savedOrder);
    }

    // 같은 이메일·배송일의 기존 PLACED 주문(배송 묶음)과 주소·우편번호가 다르면 409 ADDRESS_MISMATCH
    // 기존 주문이 모두 취소됐다면 PLACED가 없으므로 새 주소로 주문 가능
    private void validateSameAddress(String email, LocalDate shippingDate, String address, String postcode) {
        List<Order> placedOrders = orderRepository.findByEmailAndShippingDateAndStatus(email, shippingDate, OrderStatus.PLACED);

        for (Order placed : placedOrders) {
            // 앞뒤 공백 제거 후 비교, 중간 표기·대소문자는 그대로 비교
            boolean sameAddress = placed.getAddress().strip().equals(address.strip());
            boolean samePostcode = placed.getPostcode().strip().equals(postcode.strip());
            if (!sameAddress || !samePostcode) {
                throw new BusinessLogicException(ErrorCode.ADDRESS_MISMATCH);
            }
        }
    }

    @Transactional
    public OrderResponseDTO reOrder(Long orderId){
        //기존 주문
        Order original = orderRepository.findById(orderId).orElseThrow(() -> new BusinessLogicException(ErrorCode.ORDER_NOT_FOUND));

        // OrderItem 하나(item)가 들어오면 그 메뉴의 id와 수량으로 새 요청 DTO를 만들어 내보내는 것.
        List<OrderItemRequestDTO> items = original.getOrderItems().stream()
                .map(item -> new OrderItemRequestDTO(item.getMenu().getId(), item.getQuantity()))
                .toList();

        return placeOrder(original.getEmail(), original.getAddress(), original.getPostcode(), items);
    }

    @Autowired
    public OrderService(OrderRepository orderRepository, MenuRepository menuRepository) {
        this.orderRepository = orderRepository;
        this.menuRepository = menuRepository;
    }

    // 현재 시각은 항상 여기서만 구함 → 접수 시각·배송일·취소 시각이 같은 기준
    // 서울 기준, 초 단위로 자름(밀리초 이하 제거)
    private LocalDateTime now() {
        return LocalDateTime.now(Order.ZONE).truncatedTo(ChronoUnit.SECONDS);
    }

    @Transactional(readOnly = true)
    public List<OrderSummary> getOrdersByEmail(OrderListRequestDTO dto){
        List<Order> orders = orderRepository.findByEmailOrderByCreatedAtDescIdDesc(dto.email());
        return orders.stream()
                .map(OrderSummary::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO getOrder(Long orderId){
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new BusinessLogicException(ErrorCode.ORDER_NOT_FOUND));
        return OrderResponseDTO.from(order);
    }

    @Transactional
    public OrderResponseDTO cancelOrder(Long orderId){
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new BusinessLogicException(ErrorCode.ORDER_NOT_FOUND));
        order.cancel(now());
        return OrderResponseDTO.from(order);
    }

    @Transactional
    public OrderResponseDTO updateMemo(Long orderId, OrderMemoUpdateRequestDTO request){
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new BusinessLogicException(ErrorCode.ORDER_NOT_FOUND));
        order.updateMemo(request.memo());
        return OrderResponseDTO.from(order);
    }
}
