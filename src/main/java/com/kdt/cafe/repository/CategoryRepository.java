package com.kdt.cafe.repository;

import com.kdt.cafe.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category,Integer> {

    boolean existsByNameAndIdNot(String name, Integer id);
}
