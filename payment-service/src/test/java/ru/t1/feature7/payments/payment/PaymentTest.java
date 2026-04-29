package ru.t1.feature7.payments.payment;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class PaymentTest {
    private static final LocalDateTime CREATED_AT = LocalDateTime.parse("2026-04-26T10:15:30");

    @Test
    void transientPaymentsWithSameBusinessFieldsAreNotEqual() {
        Payment left = payment(null, 7L, 101L, "250.00", "utility payment");
        Payment right = payment(null, 7L, 101L, "250.00", "utility payment");

        assertNotEquals(left, right);
    }

    @Test
    void persistedPaymentsWithSameIdAreEqualEvenIfMutableFieldsDiffer() {
        Payment left = payment(1L, 7L, 101L, "250.00", "utility payment");
        Payment right = payment(1L, 8L, 102L, "125.00", "updated description");

        assertEquals(left, right);
    }

    @Test
    void hashCodeDoesNotChangeWhenIdIsAssigned() {
        Payment payment = payment(null, 7L, 101L, "250.00", "utility payment");

        int beforePersist = payment.hashCode();
        payment.setId(1L);

        assertEquals(beforePersist, payment.hashCode());
    }

    private static Payment payment(Long id, Long userId, Long productId, String amount, String description) {
        return new Payment(id, userId, productId, new BigDecimal(amount), description, CREATED_AT);
    }
}
