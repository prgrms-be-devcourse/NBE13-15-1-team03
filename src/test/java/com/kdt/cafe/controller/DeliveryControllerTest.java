package com.kdt.cafe.controller;

import com.kdt.cafe.dto.DeliveryBatch;
import com.kdt.cafe.dto.DeliveryRequestDto;
import com.kdt.cafe.dto.OrderItemResponseDTO;
import com.kdt.cafe.service.DeliveryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 * 실제 DeliveryController + 모의 DeliveryService + MockMvc
 * - A14 경로, 상태 코드, DeliveryBatch JSON 모양, 쿼리 파라미터 검사를 확인합니다.
 * - 묶음 계산 규칙은 DeliveryServiceTest에서 확인합니다.
 */
@WebMvcTest(DeliveryController.class)
class DeliveryControllerTest {

    private static final LocalDate SHIPPING_DATE = LocalDate.of(2026, 11, 3);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeliveryService deliveryService;

    @Test
    @DisplayName("A14 배송 묶음 조회는 200과 DeliveryBatch 필드 9개를 모두 담은 JSON 배열을 반환한다")
    void returns_200_with_delivery_batch_fields() throws Exception {
        DeliveryBatch batch = new DeliveryBatch(
                SHIPPING_DATE, "buyer@example.com", "서울시 실습로 10 101호", "01234",
                List.of(101L, 102L), 2, 4, 22000,
                List.of(
                        new OrderItemResponseDTO(1, "Columbia Nariñó", 5000, 3, 15000),
                        new OrderItemResponseDTO(2, "Brazil Serra Do Caparaó", 7000, 1, 7000)));
        given(this.deliveryService.getOrdersByShippingDate(new DeliveryRequestDto(SHIPPING_DATE)))
                .willReturn(List.of(batch));

        this.mockMvc.perform(get("/api/v1/delivery-batches").param("shippingDate", "2026-11-03"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].length()").value(9))
                .andExpect(jsonPath("$[0].shippingDate").value("2026-11-03"))
                .andExpect(jsonPath("$[0].email").value("buyer@example.com"))
                .andExpect(jsonPath("$[0].address").value("서울시 실습로 10 101호"))
                .andExpect(jsonPath("$[0].postcode").value("01234"))   // 문자열, 맨 앞 0 유지
                .andExpect(jsonPath("$[0].orderIds[0]").value(101))
                .andExpect(jsonPath("$[0].orderIds[1]").value(102))
                .andExpect(jsonPath("$[0].orderCount").value(2))
                .andExpect(jsonPath("$[0].totalQuantity").value(4))
                .andExpect(jsonPath("$[0].totalAmount").value(22000))
                .andExpect(jsonPath("$[0].items.length()").value(2))
                .andExpect(jsonPath("$[0].items[0].length()").value(5))
                .andExpect(jsonPath("$[0].items[0].menuId").value(1))
                .andExpect(jsonPath("$[0].items[0].menuName").value("Columbia Nariñó"))
                .andExpect(jsonPath("$[0].items[0].unitPrice").value(5000))
                .andExpect(jsonPath("$[0].items[0].quantity").value(3))
                .andExpect(jsonPath("$[0].items[0].lineAmount").value(15000));

        verify(this.deliveryService).getOrdersByShippingDate(new DeliveryRequestDto(SHIPPING_DATE));
    }

    @Test
    @DisplayName("A14 해당 배송일에 묶음이 없으면 404가 아니라 200과 빈 배열을 반환한다")
    void returns_200_and_empty_array_when_no_batch() throws Exception {
        given(this.deliveryService.getOrdersByShippingDate(any())).willReturn(List.of());

        this.mockMvc.perform(get("/api/v1/delivery-batches").param("shippingDate", "2026-11-03"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    @DisplayName("A14 shippingDate가 없으면 400 VALIDATION_ERROR와 fieldErrors.shippingDate를 반환하고 서비스를 호출하지 않는다")
    void missing_shipping_date_returns_400() throws Exception {
        this.mockMvc.perform(get("/api/v1/delivery-batches"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("입력값을 확인해 주세요."))
                .andExpect(jsonPath("$.fieldErrors.shippingDate").isNotEmpty());

        verifyNoInteractions(this.deliveryService);
    }

    @Test
    @DisplayName("A14 shippingDate가 2000-01-01~2099-12-31 범위를 벗어나면 400 VALIDATION_ERROR를 반환한다")
    void out_of_range_shipping_date_returns_400() throws Exception {
        this.mockMvc.perform(get("/api/v1/delivery-batches").param("shippingDate", "1999-12-31"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.shippingDate").isNotEmpty());

        verifyNoInteractions(this.deliveryService);
    }

    @Test
    @DisplayName("A14 shippingDate가 실제 없는 날짜(2026-02-30)거나 형식이 틀리면 400 VALIDATION_ERROR를 반환한다")
    void invalid_shipping_date_returns_400() throws Exception {
        this.mockMvc.perform(get("/api/v1/delivery-batches").param("shippingDate", "2026-02-30"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.shippingDate").isNotEmpty());

        this.mockMvc.perform(get("/api/v1/delivery-batches").param("shippingDate", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.shippingDate").isNotEmpty());

        verifyNoInteractions(this.deliveryService);
    }
}
