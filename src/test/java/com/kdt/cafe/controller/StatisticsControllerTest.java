package com.kdt.cafe.controller;

import com.kdt.cafe.dto.OrderStatistics;
import com.kdt.cafe.dto.OrderStatisticsRequestDTO;
import com.kdt.cafe.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 * 실제 StatisticsController + 모의 OrderService + MockMvc
 * - A17 경로, 상태 코드, OrderStatistics JSON 모양, date 쿼리 파라미터 검사를 확인합니다.
 * - 집계 규칙은 OrderServiceStatisticsTest에서 확인합니다.
 */
@WebMvcTest(StatisticsController.class)
class StatisticsControllerTest {

    private static final LocalDate DATE = LocalDate.of(2026, 11, 3);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    @DisplayName("A17 접수일별 주문 통계는 200과 OrderStatistics 필드 6개를 모두 담은 JSON 객체를 반환한다")
    void returns_200_with_order_statistics_fields() throws Exception {
        given(this.orderService.statistics(new OrderStatisticsRequestDTO(DATE)))
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

        verify(this.orderService).statistics(new OrderStatisticsRequestDTO(DATE));
    }

    @Test
    @DisplayName("A17 date가 없으면 400 VALIDATION_ERROR와 fieldErrors.date를 반환하고 서비스를 호출하지 않는다")
    void missing_date_returns_400() throws Exception {
        this.mockMvc.perform(get("/api/v1/statistics/orders"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("입력값을 확인해 주세요."))
                .andExpect(jsonPath("$.fieldErrors.date").isNotEmpty());

        verifyNoInteractions(this.orderService);
    }

    @Test
    @DisplayName("A17 date가 2000-01-01~2099-12-31 범위를 벗어나면 400 VALIDATION_ERROR와 fieldErrors.date를 반환한다")
    void out_of_range_date_returns_400() throws Exception {
        this.mockMvc.perform(get("/api/v1/statistics/orders").param("date", "2100-01-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("입력값을 확인해 주세요."))
                .andExpect(jsonPath("$.fieldErrors.date").isNotEmpty());

        verifyNoInteractions(this.orderService);
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

        verifyNoInteractions(this.orderService);
    }
}
