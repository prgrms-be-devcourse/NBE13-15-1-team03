package com.kdt.cafe.entity;

import com.kdt.cafe.exception.BusinessLogicException;
import com.kdt.cafe.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name="orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private OrderStatus status;

    @Column(nullable = false, length = 200)
    private String address;

    @Column(nullable = false, length = 5)
    private String postcode;

    @Column(length = 200)
    private String memo;

    @Column(nullable = false)
    private LocalDate shippingDate;

    @Column()
    private LocalDateTime cancelledAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    public static final ZoneId ZONE = ZoneId.of("Asia/Seoul"); // 접수 시각·배송일·취소 마감 모두 서울 기준
    public static final LocalTime SHIPPING_CUTOFF = LocalTime.of(14,0); // 14:00:00 상수로 고정

    protected Order() {

    }

    public Order(String email, String address, String postcode, LocalDateTime createdAt, LocalDate shippingDate) {
        this.email = email;
        this.address = address;
        this.postcode = postcode;
        this.memo = "";
        this.createdAt = createdAt;
        this.status = OrderStatus.PLACED;
        this.shippingDate = shippingDate;
        this.cancelledAt = null;
    }

    public void addOrderItems(OrderItem orderItem){
        if (orderItem == null) {
            throw new IllegalArgumentException("주문 항목은 null일 수 없습니다.");
        }
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public int getTotalAmount(){
        return this.orderItems.stream()
                .mapToInt(OrderItem::getLineAmount)
                .sum();
    }

    public int getTotalQuantity(){
        return this.orderItems.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
    }

    public void cancel(LocalDateTime now){
        // 취소면 리턴
        if (this.status == OrderStatus.CANCELLED) {
            return;
        }
        // 배송일 14:00 이후면 취소 불가
        LocalDateTime cancelDeadline =  shippingDate.atTime(SHIPPING_CUTOFF); //atTime(14:00) shippingDate에 14:00라는 시각을 더해줌.
        if (!now.isBefore(cancelDeadline)){ //지금이 마감 이전이 아니라면
            throw new BusinessLogicException(ErrorCode.ORDER_NOT_CANCELLABLE);
        }
        this.status = OrderStatus.CANCELLED;
        this.cancelledAt = now;
    }

}
