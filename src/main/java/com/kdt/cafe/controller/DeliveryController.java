package com.kdt.cafe.controller;

import com.kdt.cafe.dto.DeliveryBatch;
import com.kdt.cafe.dto.DeliveryRequestDto;
import com.kdt.cafe.service.DeliveryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
