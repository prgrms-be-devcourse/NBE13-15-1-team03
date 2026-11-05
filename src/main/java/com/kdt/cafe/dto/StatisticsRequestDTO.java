package com.kdt.cafe.dto;

import com.kdt.cafe.utils.validate.ValidDate;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record StatisticsRequestDTO(
        @NotNull(message = "날짜 입력은 필수입니다")
        @ValidDate(message = "주문일은 2000-01-01부터 2099-12-31 사이여야 합니다.")
        LocalDate date) {

}
