package com.kdt.cafe.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name="orders")
@EntityListeners(AuditingEntityListener.class)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String email;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, length = 16)
    private String status;

    @Column(nullable = false, length = 200)
    private String address;

    @Column(nullable = false, length = 5)
    private String postcode;

    @Column(length = 200)
    private String memo;

    @Column(nullable = false)
    private LocalDate shippingDate;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    protected Order() {

    }

    public Order(String email, String address, String postcode, String memo, LocalDateTime createdAt, String status, LocalDate shippingDate) {
        this.email = email;
        this.address = address;
        this.postcode = postcode;
        this.memo = memo;
        this.createdAt = createdAt;
        this.status = status;
        this.shippingDate = shippingDate;
    }

    public void addOrderItems(OrderItem orderItem){
        if (orderItem == null) {
            throw new IllegalArgumentException("주문 항목은 null일 수 없습니다.");
        }
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
