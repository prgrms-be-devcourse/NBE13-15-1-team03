package com.kdt.cafe.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequestDTO(
        @NotBlank(message="카테고리명은 비어 있을 수 없습니다.")
        @Size(max=30, message="카테고리명은 30자 이하여야 합니다.")
        String name
) {
    public CategoryRequestDTO { //공백제거
        if (name != null) {
            name = name.strip();
        }
    }
}
    //request.name() 으로 부르기
    //이름줄 괄호안에 필드 넣기. 몸통아님.

