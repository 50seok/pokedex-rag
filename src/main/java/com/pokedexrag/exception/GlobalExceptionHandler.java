package com.pokedexrag.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;

/**
 * {@link ResponseEntityExceptionHandler}를 상속해 Spring MVC 표준 예외(405/415/400 등)는
 * 원래 상태코드를 그대로 유지하면서, 응답 바디만 {@code {timestamp,code,message}} 형식으로 통일한다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String INVALID_REQUEST_CODE = "C000";
    private static final String INVALID_REQUEST_MESSAGE = "요청 값이 올바르지 않습니다";

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity.status(errorCode.httpStatus())
                .body(new ErrorResponse(LocalDateTime.now(), errorCode.code(), errorCode.message()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception e) {
        // DB 커넥션 오류 등 CustomException으로 감싸지 않은 예외가 비표준 에러 응답으로 새어나가지 않도록 하는 최종 방어선
        ErrorCode errorCode = ErrorCode.INTERNAL_ERROR;
        return ResponseEntity.status(errorCode.httpStatus())
                .body(new ErrorResponse(LocalDateTime.now(), errorCode.code(), errorCode.message()));
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                               HttpStatusCode statusCode, WebRequest request) {
        String message = ex instanceof MethodArgumentNotValidException manve
                ? extractFieldMessage(manve)
                : INVALID_REQUEST_MESSAGE;
        return ResponseEntity.status(statusCode)
                .body(new ErrorResponse(LocalDateTime.now(), INVALID_REQUEST_CODE, message));
    }

    private String extractFieldMessage(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        return fieldError != null && fieldError.getDefaultMessage() != null
                ? fieldError.getDefaultMessage()
                : INVALID_REQUEST_MESSAGE;
    }

    private record ErrorResponse(LocalDateTime timestamp, String code, String message) {
    }
}
