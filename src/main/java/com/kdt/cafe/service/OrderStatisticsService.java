package com.kdt.cafe.service;

import com.kdt.cafe.dto.MenuStatistics;
import com.kdt.cafe.dto.OrderStatistics;
import com.kdt.cafe.dto.StatisticsRequestDTO;
import com.kdt.cafe.entity.Order;
import com.kdt.cafe.entity.OrderItemAggregation;
import com.kdt.cafe.entity.OrderStatus;
import com.kdt.cafe.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// 주문 도메인의 조회 전용 집계 서비스 (상태를 바꾸지 않으므로 OrderRepository를 직접 사용)
@Service
@Transactional(readOnly = true)
public class OrderStatisticsService {

    private final OrderRepository orderRepository;

    public OrderStatisticsService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // A17 접수일별 주문 통계
    public OrderStatistics orderAnalytics(StatisticsRequestDTO dto) {
        LocalDate date = dto.date();
        List<Order> orders = findOrdersCreatedOn(date);

        int placedOrderCount = 0;
        int cancelledOrderCount = 0;
        int totalQuantity = 0;
        long totalAmount = 0;

        for (Order order : orders) {
            if (order.getStatus() == OrderStatus.CANCELLED) {
                cancelledOrderCount += 1;
                continue;
            }
            placedOrderCount += 1;
            totalQuantity += order.getTotalQuantity();
            totalAmount += order.getTotalAmount();
        }

        return new OrderStatistics(
                date,
                orders.size(),
                placedOrderCount,
                cancelledOrderCount,
                totalQuantity,
                totalAmount);
    }

    // A18 접수일별 메뉴 통계 (PLACED 주문만, 주문 당시 메뉴명·단가 기준으로 합산)
    public List<MenuStatistics> menuAnalytics(StatisticsRequestDTO dto) {
        List<Order> orders = findOrdersCreatedOn(dto.date());

        List<Order> placedOrders = new ArrayList<>();
        for (Order order : orders) {
            if (order.getStatus() == OrderStatus.PLACED) {
                placedOrders.add(order);
            }
        }

        OrderItemAggregation aggregation = OrderItemAggregation.of(placedOrders);

        List<MenuStatistics> result = new ArrayList<>();
        for (Map.Entry<OrderItemAggregation.Key, Integer> entry : aggregation.quantityByKey().entrySet()) {
            result.add(MenuStatistics.from(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    // 서울 기준 접수일(00:00:00 이상 ~ 다음 날 00:00:00 미만)의 주문을 항목과 함께 조회
    private List<Order> findOrdersCreatedOn(LocalDate date) {
        return orderRepository.findAllByCreatedDate(date.atStartOfDay(), date.plusDays(1).atStartOfDay());
    }
}
