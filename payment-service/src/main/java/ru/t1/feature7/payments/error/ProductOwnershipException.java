package ru.t1.feature7.payments.error;

public class ProductOwnershipException extends RuntimeException {
    public ProductOwnershipException(String message) {
        super(message);
    }
}
