package com.kdt.cafe.controller;


import com.kdt.cafe.dto.menu.MenuResponseDTO;
import com.kdt.cafe.service.MenuService;

import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryMenuController {

    private final MenuService menuService;

    public CategoryMenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/{categoryId}/menus")
    public List<MenuResponseDTO> menusByCategory(@PathVariable ("categoryId") @Min(value = 1, message = "카테고리 ID는 1 이상이어야 합니다.") Integer categoryId) {
        return this.menuService.findByCategory(categoryId);
    }
}
