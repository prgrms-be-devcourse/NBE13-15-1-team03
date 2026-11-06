package com.kdt.cafe.entity;


import jakarta.persistence.*;

@Entity
@Table (name = "menu")
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 300)
    private String description;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private boolean available;


    public Menu() {

    }

    public Menu(Category category, String name, int price, String description) {
        this.category = category;
        this.description = description;
        this.name = name;
        this.price = price;
        this.available = true;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public boolean isAvailable() {
        return available;
    }

    public int getId() {
        return id;
    }

    public Category getCategory() {
        return category;
    }


    public void update(Category category, String name, int price, String description) {
        this.category = category;
        this.description = description;
        this.name = name;
        this.price = price;
    }


    public void changeAvailability(boolean available) {
        this.available = available;
    }
}
