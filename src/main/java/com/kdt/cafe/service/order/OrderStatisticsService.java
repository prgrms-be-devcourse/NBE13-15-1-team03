package com.kdt.cafe.service.order;

import com.kdt.cafe.dto.statistics.CustomerStatistics;
import com.kdt.cafe.dto.statistics.MenuStatistics;
import com.kdt.cafe.dto.statistics.OrderStatistics;
import com.kdt.cafe.dto.statistics.StatisticsRequestDTO;
import com.kdt.cafe.entity.order.Order;
import com.kdt.cafe.entity.order.OrderItemAggregation;
import com.kdt.cafe.entity.order.OrderStatus;
import com.kdt.cafe.repository.OrderRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
        List<Order> orders = findOrdersCreatedOn(dto.date());
        return calculate(dto.date(), orders);
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

    // A24 접수일별 고객 통계 (이메일 오름차순·대소문자 구분, 모두 취소한 고객도 포함)
    public List<CustomerStatistics> customerAnalytics(StatisticsRequestDTO dto) {
        List<Order> orders = findOrdersCreatedOn(dto.date());
        Map<String, List<Order>> mappedByEmail = new TreeMap<>();

        for (Order order : orders) {
            if (!mappedByEmail.containsKey(order.getEmail())) {
                mappedByEmail.put(order.getEmail(), new ArrayList<>());
            }

            mappedByEmail.get(order.getEmail()).add(order);
        }

        List<CustomerStatistics> result = new ArrayList<>();

        for (Map.Entry<String, List<Order>> entry : mappedByEmail.entrySet()) {
            OrderStatistics statistics = calculate(dto.date(), entry.getValue());
            result.add(CustomerStatistics.from(entry.getKey(), statistics));
        }
        return result;
    }

    // 주문 목록 → 전체 건수(취소 포함)·상태별 건수, 수량·금액은 PLACED만 합산 (A17·A24 공통)
    private OrderStatistics calculate(LocalDate date, List<Order> orders) {
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

    // 서울 기준 접수일(00:00:00 이상 ~ 다음 날 00:00:00 미만)의 주문을 항목과 함께 조회
    private List<Order> findOrdersCreatedOn(LocalDate date) {
        return orderRepository.findAllByCreatedDate(date.atStartOfDay(), date.plusDays(1).atStartOfDay());
    }
}
