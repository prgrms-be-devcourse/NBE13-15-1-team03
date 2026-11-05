package com.kdt.cafe.config;

import com.kdt.cafe.dto.category.CategoryRequestDTO;
import com.kdt.cafe.dto.category.CategoryResponseDTO;
import com.kdt.cafe.dto.menu.MenuCreateRequestDTO;
import com.kdt.cafe.dto.menu.MenuResponseDTO;
import com.kdt.cafe.service.CategoryService;
import com.kdt.cafe.service.MenuService;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class DataInitializer implements ApplicationRunner {

    private final CategoryService categoryService;
    private final MenuService menuService;

    public DataInitializer(CategoryService categoryService, MenuService menuService) {
        this.categoryService = categoryService;
        this.menuService = menuService;
    }

    @Override
    public void run(ApplicationArguments args) {
        // 1. 카테고리: 없으면 등록, 있으면 기존 id 사용
        int singleOriginId = findOrCreateCategory("싱글오리진");
        findOrCreateCategory("블렌드");

        // 2. 메뉴: 이미 있는 이름은 건너뛰기
        Set<String> existingMenus = menuService.findAll().stream()
                .map(MenuResponseDTO::name)
                .collect(Collectors.toSet());

        createMenuIfAbsent(existingMenus, singleOriginId, "Columbia Nariñó", 5000,
                "콜롬비아 나리뇨 원두를 담은 커피빈 패키지입니다.");
        createMenuIfAbsent(existingMenus, singleOriginId, "Brazil Serra Do Caparaó", 7000,
                "브라질 세하 두 카파라오 원두를 담은 커피빈 패키지입니다.");
        createMenuIfAbsent(existingMenus, singleOriginId, "Columbia Quindío (White Wine Extended Fermentation)", 9000,
                "콜롬비아 킨디오 원두를 담은 커피빈 패키지입니다.");
        createMenuIfAbsent(existingMenus, singleOriginId, "Ethiopia Sidamo", 11000,
                "에티오피아 시다모 원두를 담은 커피빈 패키지입니다.");
    }

    // 이름으로 카테고리를 찾고, 없으면 등록해서 실제 id를 돌려줌
    private int findOrCreateCategory(String name) {
        return categoryService.findAll().stream()
                .filter(category -> category.name().equals(name))
                .map(CategoryResponseDTO::id)
                .findFirst()
                .orElseGet(() -> categoryService.save(new CategoryRequestDTO(name)).id());
    }

    // 같은 이름의 메뉴가 없을 때만 등록 (새 메뉴는 available=true로 생성됨)
    private void createMenuIfAbsent(Set<String> existingMenus, int categoryId,
                                    String name, int price, String description) {
        if (existingMenus.contains(name)) {
            return;
        }
        menuService.create(new MenuCreateRequestDTO(categoryId, name, price, description));
    }
}