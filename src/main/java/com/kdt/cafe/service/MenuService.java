package com.kdt.cafe.service;

import com.kdt.cafe.dto.MenuAvailabilityUpdateRequestDTO;
import com.kdt.cafe.dto.MenuCreateRequestDTO;
import com.kdt.cafe.dto.MenuResponseDTO;
import com.kdt.cafe.dto.MenuUpdateRequestDTO;
import com.kdt.cafe.entity.Category;
import com.kdt.cafe.entity.Menu;
import com.kdt.cafe.repository.CategoryRepository;
import com.kdt.cafe.repository.MenuRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.kdt.cafe.exception.NotfoundException;

import java.util.ArrayList;
import java.util.List;

@Service
public class MenuService {

    private final MenuRepository menuRepository;
    private final CategoryRepository categoryRepository;

    public MenuService(MenuRepository menuRepository, CategoryRepository categoryRepository) {
        this.menuRepository = menuRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public MenuResponseDTO create(MenuCreateRequestDTO request) {
        Category category = this.categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new NotfoundException("카테고리를 찾을 수 없습니다."));

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
                .orElseThrow(() -> new NotfoundException("메뉴를 찾을 수 없습니다."));
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

    @Transactional
    public List<MenuResponseDTO> findByCategory(Integer categoryId) {
        if(!this.categoryRepository.existsById(categoryId)){
            throw new NotfoundException("카테고리를 찾을 수 없습니다.");
        }

        return toResponses(this.menuRepository.findByCategory_IdOrderByIdAsc(categoryId));
    }

    @Transactional
    public MenuResponseDTO update(Integer menuId, MenuUpdateRequestDTO request) {
        Menu menu = this.findMenu(menuId);
        Category category = this.categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new NotfoundException("카테고리를 찾을 수 없습니다."));
        menu.update(category,request.name(), request.price(), request.description());
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
            throw new IllegalArgumentException("검색어를 확인해 주세요.");//임시 예외 팀 400예외가 정해지면 수정하겠습니다
        }
        return toResponses(this.menuRepository.findByNameContainingOrderByIdAsc(keyword.strip()));
    }

    @Transactional
    public void delete(Integer menuId) {
        Menu menu = this.findMenu(menuId);
//     삭제 OrderItemRepository 가 만들어지면 구현할 예정
    this.menuRepository.delete(menu);
   }



}

