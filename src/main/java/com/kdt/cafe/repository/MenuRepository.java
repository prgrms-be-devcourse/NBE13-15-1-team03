package com.kdt.cafe.repository;

import com.kdt.cafe.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Integer> {
    List<Menu> findAllByOrderByIdAsc();
}
