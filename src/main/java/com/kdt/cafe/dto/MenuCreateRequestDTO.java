package com.kdt.cafe.dto;


import jakarta.validation.constraints.*;

public record MenuCreateRequestDTO(
        @NotNull(message = "카테고리 ID를 입력해 주세요.")
        @Min(value = 1, message = "카테고리 ID는 1 이상이어야 합니다") Integer categoryId,
        @NotBlank(message = "메뉴명을 입력해 주세요.")
        @Size(max = 100, message = "메뉴명은 100자 이하로 입력해 주세요.")
        String name,
        @NotNull(message = "금액을 입력해 주세요.")
        @Min(value = 1,message = "금액은 1원 이상이어야 합니다.")
        @Max(value = 1_000_000,message = "금액은 1,000,000원 이하이어야 합니다.")
        Integer price,
        @NotNull(message = "설명은 필수 입니다. 내용이 없다면 빈 문자열(\"\")을 보내 주세요.")
        @Size(max = 300, message = "설명은 300자를 넘어갈 수 없습니다.")
        String description
) {
        public MenuCreateRequestDTO {
                if(name != null){
                        name = name.strip();
                }
                if(description != null){
                        description = description.strip();
                }
        }


}
