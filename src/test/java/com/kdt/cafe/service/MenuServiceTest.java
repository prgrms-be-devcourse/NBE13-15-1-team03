package com.kdt.cafe.service;

import com.kdt.cafe.dto.MenuCreateRequestDTO;
import com.kdt.cafe.dto.MenuResponseDTO;
import com.kdt.cafe.entity.Category;
import com.kdt.cafe.entity.Menu;
import com.kdt.cafe.exception.NotfoundException;
import com.kdt.cafe.repository.CategoryRepository;
import com.kdt.cafe.repository.MenuRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class MenuServiceTest {
    private MenuRepository menuRepository;
    private CategoryRepository categoryRepository;
    private MenuService menuService;

    @BeforeEach
    void setUp() {
        this.menuRepository = mock(MenuRepository.class);
        this.categoryRepository = mock(CategoryRepository.class);
        this.menuService = new MenuService(this.menuRepository, this.categoryRepository);
    }

    @Test
    @DisplayName("A08 카테고리가 있으면 요청 값으로 메뉴를 만들어 저장하고, 새 메뉴는 판매 중이다")
    void create_saves_menu_with_request_values() {
        Category category = mock(Category.class);
        given(category.getId()).willReturn(1);
        given(this.categoryRepository.findById(1)).willReturn(Optional.of(category));
        given(this.menuRepository.save(any(Menu.class))).willAnswer(inv -> inv.getArgument(0));

        MenuResponseDTO response = this.menuService.create(
                new MenuCreateRequestDTO(1, "House Blend", 6000, "블렌드"));

        ArgumentCaptor<Menu> captor = ArgumentCaptor.forClass(Menu.class);
        verify(this.menuRepository).save(captor.capture());
        Menu submitted = captor.getValue();
        assertEquals("House Blend", submitted.getName());
        assertEquals("블렌드", submitted.getDescription());      // name과 뒤바뀌지 않았는지 확인
        assertSame(category, submitted.getCategory());           // 찾아온 바로 그 카테고리가 연결됐는지
        assertTrue(submitted.isAvailable());                     // 새 메뉴는 판매 중
        assertEquals(1, response.categoryId());
    }

    @Test
    @DisplayName("A08 카테고리가 없으면 예외를 던지고 저장하지 않는다")
    void create_rejects_missing_category_without_saving() {
        given(this.categoryRepository.findById(99)).willReturn(Optional.empty());

        NotfoundException error = assertThrows(NotfoundException.class,
                () -> this.menuService.create(new MenuCreateRequestDTO(99, "House Blend", 6000, "블렌드")));

        assertEquals("카테고리를 찾을 수 없습니다.", error.getMessage());
        verifyNoInteractions(this.menuRepository);               // 저장 시도 자체가 없어야 함
    }
}