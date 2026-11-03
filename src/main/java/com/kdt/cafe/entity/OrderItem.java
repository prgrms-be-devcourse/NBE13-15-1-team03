package com.kdt.cafe.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, length = 100)
    private String menuName;

    @Column(nullable = false)
    private int unitPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    protected OrderItem() {

    }

    public OrderItem(Menu menu, int quantity, String menuName, int price) {
        this.menu = menu;
        this.quantity = quantity;
        this.menuName = menuName;
        this.unitPrice = price;
    }

    void setOrder(Order order) { //같은 패키지 내에서만 호출가능.
        this.order = order;
    }
}
