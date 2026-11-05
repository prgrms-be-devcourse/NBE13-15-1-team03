package com.kdt.cafe.controller;

import com.kdt.cafe.dto.OrderStatistics;
import com.kdt.cafe.dto.OrderStatisticsRequestDTO;
import com.kdt.cafe.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/statistics")
public class StatisticsController {
    private final OrderService orderService;

    public StatisticsController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/orders")
    @ResponseStatus(HttpStatus.OK)
    public OrderStatistics orderAnalytics(@Valid OrderStatisticsRequestDTO dto){
        return orderService.statistics(dto);
    }
}
