package com.kdt.cafe.controller.order;

import com.kdt.cafe.dto.delivery.DeliveryBatch;
import com.kdt.cafe.dto.delivery.DeliveryRequestDto;
import com.kdt.cafe.service.order.DeliveryService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/delivery-batches")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public List<DeliveryBatch> searchDeliveryBatch(@Valid DeliveryRequestDto dto) {
        return deliveryService.getOrdersByShippingDate(dto);
    }

}
