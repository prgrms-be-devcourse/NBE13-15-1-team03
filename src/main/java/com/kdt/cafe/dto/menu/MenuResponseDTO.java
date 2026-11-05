package com.kdt.cafe.dto.menu;

import com.kdt.cafe.entity.Menu;

public record MenuResponseDTO(
        Integer id,
        Integer categoryId,
        String name,
        Integer price,
        String description,
        boolean available


) {
    public static MenuResponseDTO from(Menu menu) {
        return new MenuResponseDTO(
                menu.getId(),
                menu.getCategory().getId(),
                menu.getName(),
                menu.getPrice(),
                menu.getDescription(),
                menu.isAvailable()
        );
    }

}
