package com.kdt.cafe.service;

import com.kdt.cafe.dto.OrderCreateRequestDTO;
import com.kdt.cafe.dto.OrderItemRequestDTO;
import com.kdt.cafe.dto.OrderResponseDTO;
import com.kdt.cafe.entity.Menu;
import com.kdt.cafe.entity.Order;
import com.kdt.cafe.exception.NotfoundException;
import com.kdt.cafe.repository.MenuRepository;
import com.kdt.cafe.repository.OrderRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuRepository menuRepository;

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final LocalTime SHIPPING_CUTOFF = LocalTime.of(14,0); // 14:00:00 상수로 고정
    private final ResponseEntityExceptionHandler responseEntityExceptionHandler;

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
            throw new NotfoundException("menu missing");
        }

        // 4. 판매 중지 메뉴가 있는지 검사
        for (Menu menu : menus){
            if (!menu.isAvailable()){
                throw new IllegalStateException("판매 중지된 메뉴가 포함되어 있습니다."); // TODO: 409 MENU_UNAVAILABLE 예외로 교체
            }
        }

        // 5. 현재 시각 구하고, 배송일 계산
        LocalDateTime nowTime = LocalDateTime.now(SEOUL);
        LocalDate shippingDate = calculateShippingDate(nowTime);

        // 6. Order 만들고, 항목마다 OrderItem 만들어서 추가
        Order order = new Order(request.email(), request.address(), request.postcode(), request.memo(), nowTime, shippingDate);

        // 7. 저장하고, 응답 DTO로 바꿔서 반환

        return null;
    }

    //배송일 계산을 위한 메서드
    private LocalDate calculateShippingDate(LocalDateTime orderAt) {
        LocalDate orderDate = orderAt.toLocalDate(); //날짜
        LocalTime orderTime = orderAt.toLocalTime(); //시간

        if (orderTime.isBefore(SHIPPING_CUTOFF)){ // 14:00:00 이전일 경우에 true 즉, 13:59:59는 true
            return orderDate; // 14:00 전 -> 당일
        }
        return orderDate.plusDays(1); // 14:00 정각부터 -> 다음 날
    }


}
