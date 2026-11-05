package com.kdt.cafe.repository;

import com.kdt.cafe.entity.Order;
import com.kdt.cafe.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    //같은 이메일로 주문한 모든 주문 가져오기
    List<Order> findByEmail(String email);
    //같은 배송 날짜의 특정 상태 주문을 항목과 함께 가져오기
    @Query("select distinct o from Order o join fetch o.orderItems where o.shippingDate = :shippingDate and o.status = :status order by o.id asc")
    List<Order> findByShippingDateAndStatus(LocalDate shippingDate, OrderStatus status);
    //같은 접수일의 모든 주문 가져오기
    @Query("select o from Order o join fetch o.orderItems where o.createdAt >= :start and o.createdAt < :end")
    List<Order> findAllByCreatedDate(LocalDateTime start, LocalDateTime end);
}
