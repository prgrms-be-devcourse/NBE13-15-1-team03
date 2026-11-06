package com.kdt.cafe.controller;


import com.kdt.cafe.dto.menu.*;
import com.kdt.cafe.service.MenuService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api/v1/menus")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    public List<MenuResponseDTO> list() {
        return this.menuService.findAll();
    }

    @PostMapping
    public ResponseEntity<MenuResponseDTO> create(@Valid @RequestBody MenuCreateRequestDTO request) {
        MenuResponseDTO response = this.menuService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{menuId}")
    public MenuResponseDTO one(@PathVariable("menuId")
                                   @Min(value = 1, message = "메뉴 ID는 1 이상이어야 합니다.") Integer menuId) {
        return this.menuService.findOne(menuId);
    }

    @GetMapping("/available")
    public List<MenuResponseDTO> available() {
        return this.menuService.findAvailable();
    }

    @GetMapping("/search")
    public List<MenuResponseDTO> search(@RequestParam(value = "keyword", required = false) String keyword) {
        return this.menuService.search(keyword);
    }

    @PutMapping("/{menuId}")
    public MenuResponseDTO update(@PathVariable("menuId")
                                      @Min(value = 1, message = "메뉴 ID는 1 이상이어야 합니다.") Integer menuId,
                                  @Valid @RequestBody MenuUpdateRequestDTO request) {
        return this.menuService.update(menuId, request);
    }

    @PutMapping("/{menuId}/availability")
    public MenuResponseDTO changeAvailability(@PathVariable("menuId")
                                                  @Min(value = 1, message = "메뉴 ID는 1 이상이어야 합니다.") Integer menuId,
                                                @Valid @RequestBody MenuAvailabilityUpdateRequestDTO request) {
        return this.menuService.changeAvailability(menuId, request);
    }

    @DeleteMapping("/{menuId}")
    public ResponseEntity<Void> delete(@PathVariable("menuId")
                                           @Min(value = 1, message = "메뉴 ID는 1 이상이어야 합니다.") Integer menuId) {
        this.menuService.delete(menuId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/page")
    public MenuPageResponseDTO page(
            @RequestParam(value = "page", defaultValue = "0")
            @Min(value = 0, message = "page는 0 이상이어야 합니다.") Integer page,
            @RequestParam(value = "size", defaultValue = "10")
            @Min(value = 1, message = "size는 1 이상이어야 합니다.")
            @Max(value = 50, message = "size는 50 이하여야 합니다.") Integer size) {
        return this.menuService.findPage(page, size);
    }
}
