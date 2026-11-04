package com.kdt.cafe.service;

import com.kdt.cafe.dto.*;
import com.kdt.cafe.entity.Menu;
import com.kdt.cafe.entity.Order;
import com.kdt.cafe.entity.OrderItem;
import com.kdt.cafe.exception.BusinessLogicException;
import com.kdt.cafe.exception.ErrorCode;
import com.kdt.cafe.repository.MenuRepository;
import com.kdt.cafe.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.servlet.filter.OrderedRequestContextFilter;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuRepository menuRepository;

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    @Transactional
    public OrderResponseDTO createOrder(OrderCreateRequestDTO request) {

        // 1. 요청에서 메뉴 id 목록 뽑기
        List<Integer> menuIds = request.items().stream()
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
        LocalDateTime nowTime = LocalDateTime.now(SEOUL);
        LocalDate shippingDate = calculateShippingDate(nowTime);

        // 6. Order 만들고, 항목마다 OrderItem 만들어서 추가
        Order order = new Order(request.email(), request.address(), request.postcode(), nowTime, shippingDate);

        Map<Integer, Menu> menuById = menus.stream()
                .collect(Collectors.toMap(Menu::getId,menu -> menu));

        for (OrderItemRequestDTO itemRequest : request.items()){
            Menu menu = menuById.get(itemRequest.menuId());
            OrderItem orderItem = new OrderItem(menu, itemRequest.quantity(), menu.getName(), menu.getPrice());
            order.addOrderItems(orderItem);
        }

        // 7. 저장하고, 응답 DTO로 바꿔서 반환
        Order savedOrder = orderRepository.save(order);
        return OrderResponseDTO.from(savedOrder);
    }

    //배송일 계산을 위한 메서드
    private LocalDate calculateShippingDate(LocalDateTime orderAt) {
        LocalDate orderDate = orderAt.toLocalDate(); //날짜
        LocalTime orderTime = orderAt.toLocalTime(); //시간

        if (orderTime.isBefore(Order.SHIPPING_CUTOFF)){ // 14:00:00 이전일 경우에 true 즉, 13:59:59는 true
            return orderDate; // 14:00 전 -> 당일
        }
        return orderDate.plusDays(1); // 14:00 정각부터 -> 다음 날
    }

    @Autowired
    public OrderService(OrderRepository orderRepository, MenuRepository menuRepository) {
        this.orderRepository = orderRepository;
        this.menuRepository = menuRepository;
    }

    @Transactional
    public List<OrderSummary> getOrdersByEmail(OrderListRequestDTO dto){
        List<Order> orders = orderRepository.findByEmail(dto.email());
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
        LocalDateTime now = LocalDateTime.now(SEOUL);
        order.cancel(now);
        return OrderResponseDTO.from(order);
    }

}
