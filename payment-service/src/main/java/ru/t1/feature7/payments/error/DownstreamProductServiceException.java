package ru.t1.feature7.payments.error;

import org.springframework.http.HttpStatus;

public class DownstreamProductServiceException extends RuntimeException {
    private final HttpStatus status;

    public DownstreamProductServiceException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
