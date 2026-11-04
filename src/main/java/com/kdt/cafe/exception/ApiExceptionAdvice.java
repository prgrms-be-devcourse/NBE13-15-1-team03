package com.kdt.cafe.exception;

import com.kdt.cafe.dto.ApiError;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionAdvice {

    @ExceptionHandler(BusinessLogicException.class)
    public ResponseEntity<ApiError> handleBusinessException(BusinessLogicException ex) {
        ErrorCode code = ex.getCode();
        ApiError response = new ApiError(code.name(), code.getMessage(), ex.getFieldErrors());
        return ResponseEntity.status(code.getStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ApiError response = new ApiError(ErrorCode.VALIDATION_ERROR.name(), ErrorCode.VALIDATION_ERROR.getMessage(), fieldErrors);

        return ResponseEntity
                .status(ErrorCode.VALIDATION_ERROR.getStatus())
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpectedException(Exception e) {
        ApiError response = new ApiError(
                ErrorCode.UNDEFINED_ERROR.name(),
                ErrorCode.UNDEFINED_ERROR.getMessage(),
                Map.of());

        return ResponseEntity
                .status(ErrorCode.UNDEFINED_ERROR.getStatus())
                .body(response);
    }
}
