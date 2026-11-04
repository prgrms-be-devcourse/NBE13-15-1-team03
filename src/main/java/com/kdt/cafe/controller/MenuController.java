package com.kdt.cafe.controller;


import com.kdt.cafe.dto.MenuAvailabilityUpdateRequestDTO;
import com.kdt.cafe.dto.MenuCreateRequestDTO;
import com.kdt.cafe.dto.MenuResponseDTO;
import com.kdt.cafe.dto.MenuUpdateRequestDTO;
import com.kdt.cafe.service.MenuService;
import jakarta.validation.Valid;
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
    public MenuResponseDTO one(@PathVariable("menuId")  Integer menuId) {
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
    public MenuResponseDTO update(@PathVariable("menuId")Integer menuId,
                                  @Valid @RequestBody MenuUpdateRequestDTO request) {
        return this.menuService.update(menuId, request);
    }

    @PutMapping("/{menuId}/availability")
    public MenuResponseDTO changeAvailability(@PathVariable("menuId") Integer menuId,
                                                @Valid @RequestBody MenuAvailabilityUpdateRequestDTO request) {
        return this.menuService.changeAvailability(menuId, request);
    }

    @DeleteMapping("/{menuId}")
    public ResponseEntity<Void> delete(@PathVariable("menuId")Integer menuId) {
        this.menuService.delete(menuId);
        return ResponseEntity.noContent().build();
    }
}
