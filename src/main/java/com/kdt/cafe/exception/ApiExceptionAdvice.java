package com.kdt.cafe.exception;

import com.kdt.cafe.dto.common.ApiError;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import tools.jackson.core.JacksonException;
import tools.jackson.core.exc.StreamReadException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
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

    // - JSON 문법 오류·본문 없음: fieldErrors = {}
    // - 자료형 오류 (예: "menuId": "x"): 최상위 필드 이름으로 보고
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleNotReadableException(HttpMessageNotReadableException e) {
        Map<String, String> fieldErrors = new HashMap<>();

        if (e.getCause() instanceof JacksonException jacksonException
                && !(jacksonException instanceof StreamReadException)) {
            List<JacksonException.Reference> path = jacksonException.getPath();
            if (!path.isEmpty() && path.getFirst().getPropertyName() != null) {
                fieldErrors.put(path.getFirst().getPropertyName(), "자료형을 확인해 주세요.");
            }
        }

        return validationError(fieldErrors);
    }

    // 경로·쿼리 값을 지정한 타입으로 바꾸지 못한 경우 (예: /api/v1/orders/abc → Long 변환 실패)
    // 파라미터 이름(orderId, menuId, categoryId 등)이 그대로 fieldErrors 이름이 됨
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatchException(MethodArgumentTypeMismatchException e) {
        return validationError(Map.of(e.getName(), "자료형을 확인해 주세요."));
    }

    private ResponseEntity<ApiError> validationError(Map<String, String> fieldErrors) {
        ApiError response = new ApiError(ErrorCode.VALIDATION_ERROR.name(), ErrorCode.VALIDATION_ERROR.getMessage(), fieldErrors);
        return ResponseEntity
                .status(ErrorCode.VALIDATION_ERROR.getStatus())
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpectedException(Exception e) {
        //예상 못 한 예외가 로그 없이 사라져서 원인을 못 찾는 문제가 있어 log.error 추가했습니다.
        log.error("정의되지 않은 예외 발생", e);

        ApiError response = new ApiError(
                ErrorCode.UNDEFINED_ERROR.name(),
                ErrorCode.UNDEFINED_ERROR.getMessage(),
                Map.of());

        return ResponseEntity
                .status(ErrorCode.UNDEFINED_ERROR.getStatus())
                .body(response);
    }
}
