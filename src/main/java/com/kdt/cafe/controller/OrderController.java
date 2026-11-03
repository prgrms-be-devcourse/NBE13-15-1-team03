package com.kdt.cafe.controller;

import com.kdt.cafe.dto.OrderListRequestDTO;
import com.kdt.cafe.dto.OrderSummary;
import com.kdt.cafe.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping()
    public ResponseEntity<List<OrderSummary>> searchOrderList(OrderListRequestDTO dto){
        return ResponseEntity.ok(orderService.getOrdersByEmail(dto));
    }

}
