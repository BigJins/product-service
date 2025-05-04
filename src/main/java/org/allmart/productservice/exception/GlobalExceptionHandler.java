package org.allmart.productservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductException.class)
    public ResponseEntity<Map<String, Object>> handleProductException(ProductException ex) {
        HttpStatus status = switch (ex.getErrorCode()) {
            case PRODUCT_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case PRODUCT_ALREADY_EXISTS -> HttpStatus.CONFLICT;
            case STOCK_SHORTAGE, INVALID_STOCK_QUANTITY, PRODUCT_NAME_EMPTY -> HttpStatus.BAD_REQUEST;
        };

        return ResponseEntity.status(status).body(
                Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", status.value(),
                        "error", status.getReasonPhrase(),
                        "message", ex.getMessage()
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleOtherExceptions(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", 500,
                        "error", "Internal Server Error",
                        "message", ex.getMessage()
                )
        );
    }
}