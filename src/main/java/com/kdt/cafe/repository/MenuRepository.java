package com.kdt.cafe.repository;

import com.kdt.cafe.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Integer> {
    List<Menu> findAllByOrderByIdAsc();
    List<Menu> findByAvailableTrueOrderByIdAsc();                   // A19 판매 중
    List<Menu> findByCategory_IdOrderByIdAsc(Integer categoryId);   // A15 카테고리별
    List<Menu> findByNameContainingOrderByIdAsc(String keyword);    // A20 검색
    boolean existsByCategory_Id(Integer categoryId);                // 카테고리 담당에게 제공 (CATEGORY_IN_USE용)
}
