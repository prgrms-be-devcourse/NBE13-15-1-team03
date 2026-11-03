package com.kdt.cafe.entity;


import jakarta.persistence.*;

@Entity
@Table(name = "category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;


    @Column(nullable = false,unique = true,length=30)
    private String name;

    public Category(String name) {
        this.name = name;
    }

    //생성자, 게터
    protected Category() {}

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
