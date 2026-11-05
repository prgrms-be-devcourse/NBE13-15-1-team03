package com.kdt.cafe.dto.delivery;

import com.kdt.cafe.utils.validate.ValidDate;

import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record DeliveryRequestDto(
        @NotNull(message = "배송일을 입력해 주세요.")
        @ValidDate(message = "배송일은 2000-01-01부터 2099-12-31 사이여야 합니다.")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate shippingDate
) {
}
