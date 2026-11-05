package com.kdt.cafe.controller.order;

import com.kdt.cafe.dto.order.OrderCreateRequestDTO;
import com.kdt.cafe.dto.order.OrderListRequestDTO;
import com.kdt.cafe.dto.order.OrderSummary;
import com.kdt.cafe.entity.order.OrderStatus;
import com.kdt.cafe.exception.BusinessLogicException;
import com.kdt.cafe.exception.ErrorCode;
import com.kdt.cafe.service.order.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/*
 * 실제 OrderController + 모의 OrderService + MockMvc
 * - A12 응답 모양·이메일 검증, A11 메뉴 중복(items) 검증·ADDRESS_MISMATCH 오류 응답 형식을 확인합니다.
 * - 정렬·배송지 비교 규칙은 OrderServiceTest에서 확인합니다.
 */
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    private static final String BUYER = "buyer@example.com";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    @DisplayName("A12 이메일별 주문 목록은 200과 OrderSummary 필드 5개를 담은 배열을 서비스가 준 순서(최신순)대로 반환한다")
    void orders_by_email_returns_200_with_summary_fields() throws Exception {
        OffsetDateTime createdAt = OffsetDateTime.of(2026, 11, 3, 13, 0, 0, 0, ZoneOffset.ofHours(9));
        given(this.orderService.getOrdersByEmail(new OrderListRequestDTO(BUYER))).willReturn(List.of(
                new OrderSummary(102L, createdAt.plusHours(1), LocalDate.of(2026, 11, 4), 5000, OrderStatus.PLACED),
                new OrderSummary(101L, createdAt, LocalDate.of(2026, 11, 3), 17000, OrderStatus.CANCELLED)));

        this.mockMvc.perform(get("/api/v1/orders").param("email", BUYER))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].length()").value(5))
                .andExpect(jsonPath("$[0].id").value(102))
                .andExpect(jsonPath("$[0].createdAt").value("2026-11-03T14:00:00+09:00"))
                .andExpect(jsonPath("$[0].shippingDate").value("2026-11-04"))
                .andExpect(jsonPath("$[0].totalAmount").value(5000))
                .andExpect(jsonPath("$[0].status").value("PLACED"))
                .andExpect(jsonPath("$[1].id").value(101))
                .andExpect(jsonPath("$[1].status").value("CANCELLED"));
    }

    @Test
    @DisplayName("A12 이메일이 정규식에 맞지 않으면 400 VALIDATION_ERROR와 fieldErrors.email을 반환하고 서비스를 호출하지 않는다")
    void orders_by_email_invalid_email_returns_400() throws Exception {
        this.mockMvc.perform(get("/api/v1/orders").param("email", "buyer@example"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("입력값을 확인해 주세요."))
                .andExpect(jsonPath("$.fieldErrors.email").isNotEmpty());

        verifyNoInteractions(this.orderService);
    }

    @Test
    @DisplayName("A11 같은 menuId가 중복된 주문은 400 VALIDATION_ERROR이고, 오류 필드 이름은 명세대로 items이며 서비스를 호출하지 않는다")
    void create_order_duplicated_menu_returns_400_items() throws Exception {
        String body = """
                {
                  "email": "buyer@example.com",
                  "address": "서울시 실습로 10 101호",
                  "postcode": "01234",
                  "items": [ { "menuId": 1, "quantity": 1 }, { "menuId": 1, "quantity": 2 } ]
                }
                """;

        this.mockMvc.perform(post("/api/v1/orders").contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("입력값을 확인해 주세요."))
                .andExpect(jsonPath("$.fieldErrors.items").value("같은 메뉴를 중복해서 주문할 수 없습니다."))
                .andExpect(jsonPath("$.fieldErrors.menuIdsUnique").doesNotExist());

        verifyNoInteractions(this.orderService);
    }

    @Test
    @DisplayName("A11 같은 이메일·배송일의 기존 주문과 배송지가 다르면 409 ADDRESS_MISMATCH와 명세 메시지, 빈 fieldErrors를 반환한다")
    void create_order_address_mismatch_returns_409() throws Exception {
        given(this.orderService.createOrder(any(OrderCreateRequestDTO.class)))
                .willThrow(new BusinessLogicException(ErrorCode.ADDRESS_MISMATCH));

        String body = """
                {
                  "email": "buyer@example.com",
                  "address": "서울시 실습로 20 202호",
                  "postcode": "01234",
                  "items": [ { "menuId": 1, "quantity": 1 } ]
                }
                """;

        this.mockMvc.perform(post("/api/v1/orders").contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ADDRESS_MISMATCH"))
                .andExpect(jsonPath("$.message").value("같은 이메일과 배송일의 기존 주문과 배송지가 다릅니다."))
                .andExpect(jsonPath("$.fieldErrors").isMap())
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }
}
