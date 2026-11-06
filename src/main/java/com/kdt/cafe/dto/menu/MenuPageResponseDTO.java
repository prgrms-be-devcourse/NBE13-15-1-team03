package com.kdt.cafe.dto.menu;

import com.kdt.cafe.entity.Menu;
import org.springframework.data.domain.Page;
import java.util.ArrayList;
import java.util.List;

public record MenuPageResponseDTO(
        List<MenuResponseDTO> content,
        int number,
        int size,
        long totalElements,
        int totalPages
) {
    public static MenuPageResponseDTO from(Page<Menu> page) {
        List<MenuResponseDTO> content = new ArrayList<>();
        for (Menu menu : page.getContent()) {
            content.add(MenuResponseDTO.from(menu));
        }
        return new MenuPageResponseDTO(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
