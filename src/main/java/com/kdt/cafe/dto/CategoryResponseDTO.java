package com.kdt.cafe.dto;

import com.kdt.cafe.entity.Category;

public record CategoryResponseDTO(int id, String name) {
    public static CategoryResponseDTO from(Category category) {
        return new CategoryResponseDTO(category.getId(),category.getName());
    }
}
