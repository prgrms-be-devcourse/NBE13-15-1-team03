package com.kdt.cafe.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // 400
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST,"입력값을 확인해 주세요."),

    // 404
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다."),
    MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "메뉴를 찾을 수 없습니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND,"주문을 찾을 수 없습니다."),
    REFERENCE_POST_NOT_FOUND(HttpStatus.NOT_FOUND, "외부 게시글을 찾을 수 없습니다."),

    // 409
    CATEGORY_NAME_DUPLICATED(HttpStatus.CONFLICT,"같은 이름의 카테고리가 이미 있습니다."),
    CATEGORY_IN_USE(HttpStatus.CONFLICT, "메뉴가 등록된 카테고리는 삭제할 수 없습니다."),
    MENU_IN_USE(HttpStatus.CONFLICT, "주문에 사용된 메뉴는 삭제할 수 없습니다."),
    ADDRESS_MISMATCH(HttpStatus.CONFLICT, "같은 이메일과 배송일의 기존 주문과 배송지가 다릅니다."),
    ORDER_NOT_CANCELLABLE(HttpStatus.CONFLICT, "배송 대상일 14시 전의 접수 상태 주문만 취소할 수 있습니다."),
    MENU_UNAVAILABLE(HttpStatus.CONFLICT, "판매 중지된 메뉴가 포함되어 있습니다."),

    // 500
    UNDEFINED_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "정의되지 않은 에러입니다."),

    // 502·504 - 외부 서버 호출 실패 (선택 심화: RestClient)
    OUTSIDE_API_FAILED(HttpStatus.BAD_GATEWAY, "외부 서버를 호출하지 못했습니다."),
    OUTSIDE_API_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "외부 서버의 응답 시간이 초과되었습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
