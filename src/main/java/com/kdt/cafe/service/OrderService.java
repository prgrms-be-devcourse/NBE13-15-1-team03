package com.kdt.cafe.service;

import com.kdt.cafe.dto.OrderListRequestDTO;
import com.kdt.cafe.dto.OrderSummary;
import com.kdt.cafe.entity.Order;
import com.kdt.cafe.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<OrderSummary> getOrdersByEmail(OrderListRequestDTO dto){
        List<Order> orders = orderRepository.findByEmail(dto.email());
        return orders.stream()
                .map(OrderSummary::from)
                .toList();
    }
}
