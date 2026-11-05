package com.kdt.cafe.controller;

import com.kdt.cafe.dto.CustomerStatistics;
import com.kdt.cafe.dto.MenuStatistics;
import com.kdt.cafe.dto.OrderStatistics;
import com.kdt.cafe.dto.StatisticsRequestDTO;
import com.kdt.cafe.service.OrderStatisticsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/statistics")
public class StatisticsController {
    private final OrderStatisticsService orderStatisticsService;

    public StatisticsController(OrderStatisticsService orderStatisticsService) {
        this.orderStatisticsService = orderStatisticsService;
    }

    @GetMapping("/orders")
    @ResponseStatus(HttpStatus.OK)
    public OrderStatistics orderAnalytics(@Valid StatisticsRequestDTO dto){
        return orderStatisticsService.orderAnalytics(dto);
    }

    @GetMapping("/menus")
    @ResponseStatus(HttpStatus.OK)
    public List<MenuStatistics> menuAnalytics(@Valid StatisticsRequestDTO dto){
        return orderStatisticsService.menuAnalytics(dto);
    }

    @GetMapping("/customers")
    @ResponseStatus(HttpStatus.OK)
    public List<CustomerStatistics> customerAnalytics(@Valid StatisticsRequestDTO dto){
        return orderStatisticsService.customerAnalytics(dto);
    }


}
