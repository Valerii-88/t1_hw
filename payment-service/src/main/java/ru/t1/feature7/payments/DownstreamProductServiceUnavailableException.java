package ru.t1.feature7.payments;

public class DownstreamProductServiceUnavailableException extends RuntimeException {
    public DownstreamProductServiceUnavailableException(String message) {
        super(message);
    }
}
