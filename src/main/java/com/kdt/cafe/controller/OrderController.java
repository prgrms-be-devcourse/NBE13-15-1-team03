package com.kdt.cafe.controller;

import com.kdt.cafe.dto.OrderCreateRequestDTO;
import com.kdt.cafe.dto.OrderListRequestDTO;
import com.kdt.cafe.dto.OrderResponseDTO;
import com.kdt.cafe.dto.OrderSummary;
import com.kdt.cafe.service.OrderService;
import jakarta.persistence.GeneratedValue;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
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

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@RequestBody OrderCreateRequestDTO request){
        OrderResponseDTO response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrder(@PathVariable("orderId") Long orderId){
        return ResponseEntity.ok(orderService.getOrder(orderId));
    }

}
