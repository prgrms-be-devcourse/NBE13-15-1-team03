package com.kdt.cafe.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class BusinessLogicException extends RuntimeException {
    private final ErrorCode code;
    private final Map<String, String> fieldErrors;

    public BusinessLogicException(ErrorCode code, String name, String message) {
        super(code.getMessage());
        this.fieldErrors = Map.of(name, message);
        this.code = code;
    }

    public BusinessLogicException(ErrorCode code) {
        super(code.getMessage());
        this.fieldErrors = Map.of();
        this.code = code;
    }
}
