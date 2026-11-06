package com.kdt.cafe.controller.order;

import com.kdt.cafe.dto.order.*;
import com.kdt.cafe.service.order.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping()
    public ResponseEntity<List<OrderSummary>> searchOrderList(@Valid OrderListRequestDTO dto){
        return ResponseEntity.ok(orderService.getOrdersByEmail(dto));
    }

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@Valid @RequestBody OrderCreateRequestDTO request){
        OrderResponseDTO response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrder(@PathVariable("orderId") @Min(value = 1, message = "주문 ID는 1 이상이어야 합니다.") Long orderId){
        return ResponseEntity.ok(orderService.getOrder(orderId));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponseDTO> cancelOrder(@PathVariable("orderId") @Min(value = 1, message = "주문 ID는 1 이상이어야 합니다.") Long orderId){
        return ResponseEntity.ok(orderService.cancelOrder(orderId));
    }

    @PutMapping("/{orderId}/memo")
    public ResponseEntity<OrderResponseDTO> updateMemo(@PathVariable("orderId") @Min(value = 1, message = "주문 ID는 1 이상이어야 합니다.") Long orderId,@Valid @RequestBody OrderMemoUpdateRequestDTO request){
        return ResponseEntity.ok(orderService.updateMemo(orderId,request));
    }

    @PostMapping("/{orderId}/reorder")
    public ResponseEntity<OrderResponseDTO> reOrder(@PathVariable("orderId") @Min(value = 1, message = "주문 ID는 1 이상이어야 합니다.") Long orderId){
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.reOrder(orderId));
    }


}
