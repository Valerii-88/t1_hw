package ru.t1.feature7.payments;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class PaymentServiceExceptionHandler {
    @ExceptionHandler(DownstreamProductServiceException.class)
    public ResponseEntity<Map<String, String>> handleDownstreamProductServiceException(
            DownstreamProductServiceException exception
    ) {
        return ResponseEntity.status(exception.getStatus())
                .body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(DownstreamProductServiceUnavailableException.class)
    public ResponseEntity<Map<String, String>> handleDownstreamProductServiceUnavailableException(
            DownstreamProductServiceUnavailableException exception
    ) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("error", exception.getMessage()));
    }
}
