package ru.t1.feature7.payments;

import org.junit.jupiter.api.Test;
import ru.t1.feature7.payments.api.PaymentResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PaymentMapperTest {
    private static final LocalDateTime CREATED_AT = LocalDateTime.parse("2026-04-26T10:15:30");

    private final PaymentMapper paymentMapper = new PaymentMapper();

    @Test
    void mapsArgumentsToPaymentEntity() {
        Payment payment = paymentMapper.toEntity(7L, 101L, new BigDecimal("250.00"), "utility payment", CREATED_AT);

        assertNull(payment.getId());
        assertEquals(7L, payment.getUserId());
        assertEquals(101L, payment.getProductId());
        assertEquals(new BigDecimal("250.00"), payment.getAmount());
        assertEquals("utility payment", payment.getDescription());
        assertEquals(CREATED_AT, payment.getCreatedAt());
    }

    @Test
    void mapsPaymentToResponse() {
        Payment payment = new Payment(1L, 7L, 101L, new BigDecimal("250.00"), "utility payment", CREATED_AT);

        PaymentResponse response = paymentMapper.toResponse(payment);

        assertEquals(new PaymentResponse(1L, 7L, 101L, new BigDecimal("250.00"), "utility payment", CREATED_AT), response);
    }
}
