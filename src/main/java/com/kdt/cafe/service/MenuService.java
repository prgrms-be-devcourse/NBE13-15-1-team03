package com.kdt.cafe.service;

import com.kdt.cafe.dto.MenuAvailabilityUpdateRequestDTO;
import com.kdt.cafe.dto.MenuCreateRequestDTO;
import com.kdt.cafe.dto.MenuResponseDTO;
import com.kdt.cafe.dto.MenuUpdateRequestDTO;
import com.kdt.cafe.entity.Category;
import com.kdt.cafe.entity.Menu;
import com.kdt.cafe.repository.CategoryRepository;
import com.kdt.cafe.repository.MenuRepository;
import com.kdt.cafe.repository.OrderItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.kdt.cafe.exception.ErrorCode;
import com.kdt.cafe.exception.BusinessLogicException;

import java.util.ArrayList;
import java.util.List;

@Service
public class MenuService {

    private final MenuRepository menuRepository;
    private final CategoryRepository categoryRepository;
    private final OrderItemRepository orderItemRepository;

    public MenuService(MenuRepository menuRepository, CategoryRepository categoryRepository, OrderItemRepository orderItemRepository) {
        this.menuRepository = menuRepository;
        this.categoryRepository = categoryRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional
    public MenuResponseDTO create(MenuCreateRequestDTO request) {
        Category category = this.categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new BusinessLogicException(ErrorCode.CATEGORY_NOT_FOUND));

        Menu menu = new Menu(category, request.name(), request.price(), request.description());

        Menu saved = this.menuRepository.save(menu);

        return MenuResponseDTO.from(saved);
    }

    @Transactional(readOnly = true)
    public List<MenuResponseDTO> findAll() {
        return toResponses(this.menuRepository.findAllByOrderByIdAsc());
    }

    private Menu findMenu(Integer menuId) {
        return this.menuRepository.findById(menuId)
                .orElseThrow(() -> new BusinessLogicException(ErrorCode.MENU_NOT_FOUND));
    }

    private List<MenuResponseDTO> toResponses(List<Menu> menus){
        List<MenuResponseDTO> result = new ArrayList<>();
        for (Menu menu : menus) {
            result.add(MenuResponseDTO.from(menu));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public MenuResponseDTO findOne(Integer id) {
        Menu menu = this.findMenu(id);
        return MenuResponseDTO.from(menu);
    }

    @Transactional(readOnly = true)
    public List<MenuResponseDTO> findAvailable() {
        return toResponses(this.menuRepository.findByAvailableTrueOrderByIdAsc());
    }

    @Transactional(readOnly = true)
    public List<MenuResponseDTO> findByCategory(Integer categoryId) {
        if(!this.categoryRepository.existsById(categoryId)){
            throw new BusinessLogicException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        return toResponses(this.menuRepository.findByCategory_IdOrderByIdAsc(categoryId));
    }

    @Transactional
    public MenuResponseDTO update(Integer menuId, MenuUpdateRequestDTO request) {
        Menu menu = this.findMenu(menuId);
        Category category = this.categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new BusinessLogicException(ErrorCode.CATEGORY_NOT_FOUND));
        menu.update(category,request.name() , request.price(), request.description());
        return MenuResponseDTO.from(menu);

    }

    @Transactional
    public MenuResponseDTO changeAvailability(Integer menuId, MenuAvailabilityUpdateRequestDTO request) {
        Menu menu = this.findMenu(menuId);
        menu.changeAvailability(request.available());
        return MenuResponseDTO.from(menu);
    }

    @Transactional (readOnly = true)
    public List<MenuResponseDTO> search(String keyword) {
        if (keyword == null || keyword.strip().isEmpty() || keyword.strip().length() > 100){
            throw new BusinessLogicException(ErrorCode.VALIDATION_ERROR,
                    "keyword", "검색어는 앞뒤 공백을 제외하고 1~100자로 입력해 주세요.");
        }
        return toResponses(this.menuRepository.findByNameContainingOrderByIdAsc(keyword.strip()));
    }

    @Transactional
    public void delete(Integer menuId) {
        Menu menu = this.findMenu(menuId);
        if(this.orderItemRepository.existsByMenu_Id(menuId)) {
            throw new BusinessLogicException(ErrorCode.MENU_IN_USE);
        }
        this.menuRepository.delete(menu);
   }



}

