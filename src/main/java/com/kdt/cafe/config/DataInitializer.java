package com.kdt.cafe.config;

import com.kdt.cafe.service.CategoryService;
import com.kdt.cafe.service.OrderService;
import com.kdt.cafe.service.OrderService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {

    private final CategoryService categoryService;
    private final OrderService orderService;

    public DataInitializer(CategoryService categoryService, OrderService orderService) {
        this.categoryService = categoryService;
        this.orderService = orderService;
    }

    @Override
    public void run(ApplicationArguments args){

        int singleOriginId =
    }
}
