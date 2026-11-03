package com.kdt.cafe.dto;

import com.kdt.cafe.utils.validate.EmailValidator;

public record OrderListRequestDTO(String email) {

    public OrderListRequestDTO {
        if (email == null) {
            throw new IllegalArgumentException("이메일이 null 입니다");
        }

        if (email.length() > 100) {
            throw new IllegalArgumentException("길이 제한 초과");
        }

        if (!EmailValidator.isValid(email)) {
            throw new IllegalArgumentException("형식에 맞지 않는 이메일:" + email);
        }
    }
}
