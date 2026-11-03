package com.kdt.cafe.controller;

import com.kdt.cafe.entity.Category;
import com.kdt.cafe.service.CategoryService;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    //전체 조회
    @GetMapping
    public List<Category> getCategories() {
        return categoryService.findAll();
    }

//    //단건
//    @GetMapping("/{categoryId}")
//    public Category getCategory(@PathVariable("categoryId") int categoryId) {
//        return categoryService.findById(categoryId);
//    }
//
//    //등록
//    @PostMapping
//    public Category saveCategory(@RequestBody Category category) {
//        return categoryService.save(category);
//    }
//
//    //수정
//    @PutMapping("/{categoryId}")
//    public Category updateCategory(@PathVariable("categoryId") int categoryId, @RequestBody Category category) {
//        return categoryService.update(category);
//    }
//
//    //삭제
//    @DeleteMapping("/{categoryId}")
//    public void deleteCategory(@PathVariable("categoryId") int categoryId) {
//        return categoryService.delete();
//    }
//
//    @GetMapping("/{categoryId}/menus")
//    public List<Menu> getMenusByCategory(@PathVariable("categoryId") int categoryId) {
//        return
//    }
}
