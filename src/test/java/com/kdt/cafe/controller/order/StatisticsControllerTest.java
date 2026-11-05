package com.kdt.cafe.controller.order;

import com.kdt.cafe.dto.statistics.CustomerStatistics;
import com.kdt.cafe.dto.statistics.MenuStatistics;
import com.kdt.cafe.dto.statistics.OrderStatistics;
import com.kdt.cafe.dto.statistics.StatisticsRequestDTO;
import com.kdt.cafe.service.order.OrderStatisticsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/*
 * 실제 StatisticsController + 모의 OrderStatisticsService + MockMvc
 * - A17·A18·A24 경로, 상태 코드, 응답 JSON 모양, date 쿼리 파라미터 검사를 확인합니다.
 * - 집계 규칙은 OrderStatisticsServiceTest에서 확인합니다.
 */
@WebMvcTest(StatisticsController.class)
class StatisticsControllerTest {

    private static final LocalDate DATE = LocalDate.of(2026, 11, 3);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderStatisticsService orderStatisticsService;

    @Test
    @DisplayName("A17 접수일별 주문 통계는 200과 OrderStatistics 필드 6개를 모두 담은 JSON 객체를 반환한다")
    void returns_200_with_order_statistics_fields() throws Exception {
        given(this.orderStatisticsService.orderAnalytics(new StatisticsRequestDTO(DATE)))
                .willReturn(new OrderStatistics(DATE, 2, 1, 1, 1, 5000));

        this.mockMvc.perform(get("/api/v1/statistics/orders").param("date", "2026-11-03"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(6))
                .andExpect(jsonPath("$.date").value("2026-11-03"))
                .andExpect(jsonPath("$.orderCount").value(2))
                .andExpect(jsonPath("$.placedOrderCount").value(1))
                .andExpect(jsonPath("$.cancelledOrderCount").value(1))
                .andExpect(jsonPath("$.totalQuantity").value(1))
                .andExpect(jsonPath("$.totalAmount").value(5000));

        verify(this.orderStatisticsService).orderAnalytics(new StatisticsRequestDTO(DATE));
    }

    @Test
    @DisplayName("A17 date가 없으면 400 VALIDATION_ERROR와 fieldErrors.date를 반환하고 서비스를 호출하지 않는다")
    void missing_date_returns_400() throws Exception {
        this.mockMvc.perform(get("/api/v1/statistics/orders"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("입력값을 확인해 주세요."))
                .andExpect(jsonPath("$.fieldErrors.date").isNotEmpty());

        verifyNoInteractions(this.orderStatisticsService);
    }

    @Test
    @DisplayName("A17 date가 2000-01-01~2099-12-31 범위를 벗어나면 400 VALIDATION_ERROR와 fieldErrors.date를 반환한다")
    void out_of_range_date_returns_400() throws Exception {
        this.mockMvc.perform(get("/api/v1/statistics/orders").param("date", "2100-01-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("입력값을 확인해 주세요."))
                .andExpect(jsonPath("$.fieldErrors.date").isNotEmpty());

        verifyNoInteractions(this.orderStatisticsService);
    }

    @Test
    @DisplayName("A17 date가 실제 없는 날짜(2026-02-30)거나 형식이 틀리면 400 VALIDATION_ERROR와 fieldErrors.date를 반환한다")
    void invalid_date_returns_400() throws Exception {
        this.mockMvc.perform(get("/api/v1/statistics/orders").param("date", "2026-02-30"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.date").isNotEmpty());

        this.mockMvc.perform(get("/api/v1/statistics/orders").param("date", "20261103"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.date").isNotEmpty());

        verifyNoInteractions(this.orderStatisticsService);
    }

    // ===== A18 접수일별 메뉴 통계 =====

    @Test
    @DisplayName("A18 접수일별 메뉴 통계는 200과 MenuStatistics 필드 5개를 담은 JSON 배열을 서비스가 준 순서대로 반환한다")
    void menus_returns_200_with_menu_statistics_fields() throws Exception {
        given(this.orderStatisticsService.menuAnalytics(new StatisticsRequestDTO(DATE)))
                .willReturn(List.of(
                        new MenuStatistics(1, "Columbia Nariñó", 5000, 3, 15000),
                        new MenuStatistics(2, "Brazil Serra Do Caparaó", 7000, 1, 7000)));

        this.mockMvc.perform(get("/api/v1/statistics/menus").param("date", "2026-11-03"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].length()").value(5))
                .andExpect(jsonPath("$[0].menuId").value(1))
                .andExpect(jsonPath("$[0].menuName").value("Columbia Nariñó"))
                .andExpect(jsonPath("$[0].unitPrice").value(5000))
                .andExpect(jsonPath("$[0].totalQuantity").value(3))
                .andExpect(jsonPath("$[0].totalAmount").value(15000))
                .andExpect(jsonPath("$[1].menuId").value(2));

        verify(this.orderStatisticsService).menuAnalytics(new StatisticsRequestDTO(DATE));
    }

    @Test
    @DisplayName("A18 주문이 없는 날짜의 메뉴 통계는 404가 아니라 200과 빈 배열을 반환한다")
    void menus_returns_200_and_empty_array_when_no_orders() throws Exception {
        given(this.orderStatisticsService.menuAnalytics(new StatisticsRequestDTO(DATE))).willReturn(List.of());

        this.mockMvc.perform(get("/api/v1/statistics/menus").param("date", "2026-11-03"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    @DisplayName("A18 date가 없거나 범위를 벗어나면 400 VALIDATION_ERROR와 fieldErrors.date를 반환하고 서비스를 호출하지 않는다")
    void menus_invalid_date_returns_400() throws Exception {
        this.mockMvc.perform(get("/api/v1/statistics/menus"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("입력값을 확인해 주세요."))
                .andExpect(jsonPath("$.fieldErrors.date").isNotEmpty());

        this.mockMvc.perform(get("/api/v1/statistics/menus").param("date", "1999-12-31"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.date").isNotEmpty());

        verifyNoInteractions(this.orderStatisticsService);
    }

    // ===== A24 접수일별 고객 통계 =====

    @Test
    @DisplayName("A24 접수일별 고객 통계는 200과 CustomerStatistics 필드 7개를 담은 JSON 배열을 서비스가 준 순서대로 반환한다")
    void customers_returns_200_with_customer_statistics_fields() throws Exception {
        given(this.orderStatisticsService.customerAnalytics(new StatisticsRequestDTO(DATE)))
                .willReturn(List.of(
                        new CustomerStatistics(DATE, "alice@example.com", 1, 0, 1, 0, 0),
                        new CustomerStatistics(DATE, "buyer@example.com", 2, 1, 1, 1, 5000)));

        this.mockMvc.perform(get("/api/v1/statistics/customers").param("date", "2026-11-03"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].length()").value(7))
                .andExpect(jsonPath("$[0].date").value("2026-11-03"))
                .andExpect(jsonPath("$[0].email").value("alice@example.com"))
                .andExpect(jsonPath("$[0].placedOrderCount").value(0))
                .andExpect(jsonPath("$[0].totalAmount").value(0))
                .andExpect(jsonPath("$[1].email").value("buyer@example.com"))
                .andExpect(jsonPath("$[1].orderCount").value(2))
                .andExpect(jsonPath("$[1].placedOrderCount").value(1))
                .andExpect(jsonPath("$[1].cancelledOrderCount").value(1))
                .andExpect(jsonPath("$[1].totalQuantity").value(1))
                .andExpect(jsonPath("$[1].totalAmount").value(5000));

        verify(this.orderStatisticsService).customerAnalytics(new StatisticsRequestDTO(DATE));
    }

    @Test
    @DisplayName("A24 주문이 없는 날짜의 고객 통계는 404가 아니라 200과 빈 배열을 반환한다")
    void customers_returns_200_and_empty_array_when_no_orders() throws Exception {
        given(this.orderStatisticsService.customerAnalytics(new StatisticsRequestDTO(DATE))).willReturn(List.of());

        this.mockMvc.perform(get("/api/v1/statistics/customers").param("date", "2026-11-03"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    @DisplayName("A24 date가 없거나 실제 없는 날짜면 400 VALIDATION_ERROR와 fieldErrors.date를 반환하고 서비스를 호출하지 않는다")
    void customers_invalid_date_returns_400() throws Exception {
        this.mockMvc.perform(get("/api/v1/statistics/customers"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("입력값을 확인해 주세요."))
                .andExpect(jsonPath("$.fieldErrors.date").isNotEmpty());

        this.mockMvc.perform(get("/api/v1/statistics/customers").param("date", "2026-02-30"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.date").isNotEmpty());

        verifyNoInteractions(this.orderStatisticsService);
    }
}
