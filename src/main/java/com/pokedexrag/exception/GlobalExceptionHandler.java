package com.pokedexrag.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String INVALID_REQUEST_CODE = "C000";
    private static final String INVALID_REQUEST_MESSAGE = "요청 값이 올바르지 않습니다";

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity.status(errorCode.httpStatus())
                .body(new ErrorResponse(LocalDateTime.now(), errorCode.code(), errorCode.message()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null && fieldError.getDefaultMessage() != null
                ? fieldError.getDefaultMessage()
                : INVALID_REQUEST_MESSAGE;
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(LocalDateTime.now(), INVALID_REQUEST_CODE, message));
    }

    private record ErrorResponse(LocalDateTime timestamp, String code, String message) {
    }
}
