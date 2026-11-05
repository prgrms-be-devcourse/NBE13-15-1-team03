package com.kdt.cafe.repository;

import com.kdt.cafe.entity.order.OrderItem;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    boolean existsByMenu_Id(Integer menuId);
}
