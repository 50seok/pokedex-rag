package com.pokedexrag.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;

/**
 * {@link ResponseEntityExceptionHandler}를 상속해 Spring MVC 표준 예외(400 등)는
 * 원래 상태코드를 그대로 유지하면서, 응답 바디만 {@code {timestamp,code,message}} 형식으로 통일한다.
 * {@code annotations = RestController.class}로 스코프를 한정해 {@code @Controller}(뷰 반환) 컨트롤러의
 * 예외는 가로채지 않고 Spring Boot 기본 /error 처리(HTML 에러 페이지)로 흘려보낸다.
 *
 * <p><b>예외</b>: 405(허용 안 된 HTTP 메서드) 등 핸들러 메서드가 아직 선택되지 않은 상태에서
 * 발생하는 예외는 {@code annotations=} selector가 핸들러 타입(null)을 판별할 수 없어 이
 * advice가 아예 적용되지 않는다({@link org.springframework.web.method.HandlerTypePredicate}
 * 참고) — 이 경우 상태코드는 정확히 유지되지만 바디는 Spring Boot 기본 에러 포맷으로 나간다.
 * 현재 REST 엔드포인트가 {@code POST /api/chat} 하나뿐이라(코드리뷰로 확인, 이슈 #13) 별도
 * {@code HandlerExceptionResolver}를 추가하는 대응은 하지 않는다.
 */
@RestControllerAdvice(annotations = RestController.class)
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
