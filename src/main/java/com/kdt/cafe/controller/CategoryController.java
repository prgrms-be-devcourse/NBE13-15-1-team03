package com.kdt.cafe.controller;

import com.kdt.cafe.dto.category.CategoryRequestDTO;
import com.kdt.cafe.dto.category.CategoryResponseDTO;
import com.kdt.cafe.service.CategoryService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
    @ResponseStatus(HttpStatus.OK)
    public List<CategoryResponseDTO> getCategories() {
        return categoryService.findAll();
    }

    //단건
    @GetMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryResponseDTO getCategory(@PathVariable("categoryId") int categoryId) {
        return categoryService.findById(categoryId);
    }

    //등록
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponseDTO saveCategory(@Valid @RequestBody CategoryRequestDTO categoryRequestDTO) {
        return categoryService.save(categoryRequestDTO);
    }

    //수정
    @PutMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryResponseDTO updateCategory(@PathVariable("categoryId") int categoryId,@Valid @RequestBody CategoryRequestDTO categoryRequestDTO) {
        return categoryService.update(categoryId, categoryRequestDTO);
    }


    //삭제
    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable("categoryId") int categoryId) {
        categoryService.delete(categoryId);
    }

}
