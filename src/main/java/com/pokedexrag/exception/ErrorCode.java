package com.pokedexrag.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    CHAT_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "C001", "답변을 생성할 수 없습니다"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C999", "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    public HttpStatus httpStatus() {
        return httpStatus;
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }
}
