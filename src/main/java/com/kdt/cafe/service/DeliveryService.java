package com.kdt.cafe.service;

import com.kdt.cafe.dto.DeliveryBatch;
import com.kdt.cafe.dto.DeliveryRequestDto;
import com.kdt.cafe.dto.OrderItemResponseDTO;
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
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class DeliveryService {

    private final OrderRepository orderRepository;

    public DeliveryService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public List<DeliveryBatch> getOrdersByShippingDate(DeliveryRequestDto dto) {
        // 주문 + 항목
        List<Order> orders = orderRepository.findByShippingDateAndStatus(dto.shippingDate(), OrderStatus.PLACED);

        // TreeMap → 이메일 오름차순(대소문자 구분), 그룹 안은 id 오름차순 유지
        Map<String, List<Order>> ordersByEmail = orders.stream()
                .collect(Collectors.groupingBy(Order::getEmail, TreeMap::new, Collectors.toList()));

        List<DeliveryBatch> batches = new ArrayList<>(ordersByEmail.size());
        for (List<Order> group : ordersByEmail.values()) {
            batches.add(toBatch(dto.shippingDate(), group));
        }
        return batches;
    }

    private DeliveryBatch toBatch(LocalDate shippingDate, List<Order> group) {
        Order first = group.getFirst();
        List<Long> orderIds = new ArrayList<>(group.size());
        for (Order order : group) {
            orderIds.add(order.getId());
        }

        // 이메일 별로 그룹한 주문을 바탕으로 주문 목록 묶음을 계산
        OrderItemAggregation aggregation = OrderItemAggregation.of(group);

        List<OrderItemResponseDTO> items = new ArrayList<>();
        for (Map.Entry<OrderItemAggregation.Key, Integer> entry : aggregation.quantityByKey().entrySet()) {
            items.add(OrderItemResponseDTO.from(entry.getKey(), entry.getValue()));
        }

        return new DeliveryBatch(shippingDate, first.getEmail(), first.getAddress(), first.getPostcode(),
                orderIds, orderIds.size(), aggregation.totalQuantity(), aggregation.totalAmount(), items);
    }
}
