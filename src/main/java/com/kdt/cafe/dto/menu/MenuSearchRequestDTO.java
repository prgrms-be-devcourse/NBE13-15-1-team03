package com.kdt.cafe.dto.menu;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// A20 메뉴명 검색 조건 (?keyword=...)
// 명세: 앞뒤 공백 제거 후 1~100자 → 생성자에서 먼저 공백을 제거하고, 제거한 값으로 검증
public record MenuSearchRequestDTO(
        @NotBlank(message = "검색어를 입력해 주세요.")
        @Size(max = 100, message = "검색어는 앞뒤 공백을 제외하고 100자 이하로 입력해 주세요.")
        String keyword
) {
    public MenuSearchRequestDTO {
        if (keyword != null) {
            keyword = keyword.strip();
        }
    }
}
