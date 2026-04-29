package ru.t1.feature7.payments.error;

public class DownstreamProductServiceUnavailableException extends RuntimeException {
    public DownstreamProductServiceUnavailableException(String message) {
        super(message);
    }
}
