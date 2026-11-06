package com.kdt.cafe.dto.order;

import com.kdt.cafe.utils.validate.UniqueMenuIds;
import com.kdt.cafe.utils.validate.ValidEmail;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OrderCreateRequestDTO(
        @NotNull(message = "이메일은 필수입니다.")
        @Size(min = 1, max = 100, message = "이메일은 1자 이상 100자 이하여야 합니다.")
        @ValidEmail(message = "이메일이 형식에 맞지 않습니다")
        String email,
        @NotBlank(message = "주소는 필수입니다.")
        @Size(max = 200, message = "주소는 200자 이하여야 합니다.")
        String address,

        @NotNull(message = "우편번호는 필수입니다.")
        @Pattern(regexp = "\\d{5}", message = "우편번호는 숫자 5자리여야 합니다.")
        String postcode,

        @NotNull(message = "주문 항목은 필수입니다.")
        @Size(min = 1, max = 10, message = "주문 항목은 1개 이상 10개 이하여야 합니다.")
        @UniqueMenuIds(message = "같은 메뉴를 중복해서 주문할 수 없습니다.")
        List<@NotNull(message = "주문 항목에 빈 값(null)이 있습니다.") @Valid OrderItemRequestDTO> items
) {
        // 주소, 우편번호의 앞뒤 공백 제거
        public OrderCreateRequestDTO{
                address = (address == null) ? null : address.strip();
                postcode = (postcode == null) ? null : postcode.strip();
        }
}

