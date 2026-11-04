package com.kdt.cafe.exception;

import lombok.Getter;

@Getter
public class BusinessLogicException extends RuntimeException {
    private final ErrorCode code;
    public BusinessLogicException(ErrorCode code) {
        super(code.getMessage());
        this.code = code;
    }
}
