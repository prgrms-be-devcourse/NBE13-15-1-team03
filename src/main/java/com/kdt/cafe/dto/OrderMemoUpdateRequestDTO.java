package com.kdt.cafe.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OrderMemoUpdateRequestDTO(
        @NotNull(message = "메모는 필수입니다. 비우려면 빈 문자열을 보내주세요.")
        @Size(max = 200, message = "메모는 200자 이하여야 합니다.")
        String memo
) {
}
