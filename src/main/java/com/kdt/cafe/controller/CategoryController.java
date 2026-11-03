package com.kdt.cafe.controller;

import com.kdt.cafe.dto.CategoryRequestDTO;
import com.kdt.cafe.dto.CategoryResponseDTO;
import com.kdt.cafe.entity.Category;
import com.kdt.cafe.service.CategoryService;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
    public List<CategoryResponseDTO> getCategories() {
        return categoryService.findAll();
    }

    //단건
    @GetMapping("/{categoryId}")
    public CategoryResponseDTO getCategory(@PathVariable("categoryId") int categoryId) {
        return categoryService.findById(categoryId);
    }

    //등록
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponseDTO saveCategory(@RequestBody CategoryRequestDTO categoryRequestDTO) {
        return categoryService.save(categoryRequestDTO);
    }

    //수정
    @PutMapping("/{categoryId}")
    public CategoryResponseDTO updateCategory(@PathVariable("categoryId") int categoryId, @RequestBody CategoryRequestDTO categoryRequestDTO) {
        return categoryService.update(categoryId, categoryRequestDTO);
    }


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
