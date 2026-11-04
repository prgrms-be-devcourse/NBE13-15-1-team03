package com.kdt.cafe.controller;

import com.kdt.cafe.dto.MenuCreateRequestDTO;
import com.kdt.cafe.dto.MenuResponseDTO;
import com.kdt.cafe.service.MenuService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 * 실제 MenuController + 모의 MenuService + MockMvc
 * - 컨트롤러의 경로, 상태 코드, JSON 모양, @Valid 검사를 확인합니다.
 * - 서비스는 모의 객체라 실제 DB에는 접속하지 않습니다.
 */
@WebMvcTest(MenuController.class)
class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MenuService menuService;

    // CafeApplication의 @EnableJpaAuditing 때문에 @WebMvcTest가 JPA 정보를 찾음
    // JpaAuditingConfig로 옮기기 전까지 임시로 가짜 객체 등록
    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;


    // ===== A06 전체 메뉴 =====

    @Test
    @DisplayName("A06 전체 메뉴 조회는 200과 메뉴 JSON 배열을 반환한다")
    void list_returns_200_json_array() throws Exception {
        List<MenuResponseDTO> rows = new ArrayList<>();
        rows.add(new MenuResponseDTO(1, 1, "Columbia Narino", 5000, "colombia", true));
        rows.add(new MenuResponseDTO(2, 1, "Ethiopia Sidamo", 11000, "ethiopia", false));
        given(this.menuService.findAll()).willReturn(rows);

        this.mockMvc.perform(get("/api/v1/menus"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].categoryId").value(1))
                .andExpect(jsonPath("$[0].name").value("Columbia Narino"))
                .andExpect(jsonPath("$[0].price").value(5000))
                .andExpect(jsonPath("$[0].description").value("colombia"))
                .andExpect(jsonPath("$[0].available").value(true))
                .andExpect(jsonPath("$[0].length()").value(6))          // 명세서 Menu 필드 6개
                .andExpect(jsonPath("$[1].available").value(false));    // 판매 중지 메뉴도 목록에 포함

        verify(this.menuService).findAll();
    }

    @Test
    @DisplayName("A06 메뉴가 없으면 404가 아니라 200과 빈 배열을 반환한다")
    void empty_list_returns_200_and_empty_array() throws Exception {
        given(this.menuService.findAll()).willReturn(new ArrayList<>());

        this.mockMvc.perform(get("/api/v1/menus"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(this.menuService).findAll();
    }

    // ===== A08 메뉴 등록 =====

    @Test
    @DisplayName("A08 메뉴 등록은 201, Location 헤더, 새 메뉴 JSON을 반환하고 요청 값을 서비스에 그대로 전달한다")
    void create_returns_201_and_passes_request_values() throws Exception {
        MenuCreateRequestDTO expected = new MenuCreateRequestDTO(1, "House Blend", 6000, "blend");
        MenuResponseDTO created = new MenuResponseDTO(5, 1, "House Blend", 6000, "blend", true);
        given(this.menuService.create(expected)).willReturn(created);

        this.mockMvc.perform(post("/api/v1/menus")
                        .contentType(APPLICATION_JSON)
                        .content("{\"categoryId\":1,\"name\":\"House Blend\",\"price\":6000,\"description\":\"blend\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/menus/5"))
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.categoryId").value(1))
                .andExpect(jsonPath("$.name").value("House Blend"))
                .andExpect(jsonPath("$.price").value(6000))
                .andExpect(jsonPath("$.description").value("blend"))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.length()").value(6));

        // record라서 값이 같으면 equals가 true → JSON이 그대로 DTO로 전달됐는지 확인
        verify(this.menuService).create(expected);
    }

    @Test
    @DisplayName("A08 가격이 0이면 서비스 호출 전에 400을 반환한다")
    void create_price0_returns_400_before_service() throws Exception {
        this.mockMvc.perform(post("/api/v1/menus")
                        .contentType(APPLICATION_JSON)
                        .content("{\"categoryId\":1,\"name\":\"House Blend\",\"price\":0,\"description\":\"\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(this.menuService);
    }

    @Test
    @DisplayName("A08 categoryId가 없으면 서비스 호출 전에 400을 반환한다")
    void create_missing_categoryId_returns_400_before_service() throws Exception {
        this.mockMvc.perform(post("/api/v1/menus")
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"House Blend\",\"price\":6000,\"description\":\"blend\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(this.menuService);
    }
}