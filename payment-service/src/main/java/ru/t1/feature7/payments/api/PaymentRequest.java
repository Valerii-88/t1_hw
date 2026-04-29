package ru.t1.feature7.payments.api;

import java.math.BigDecimal;

public record PaymentRequest(
        Long userId,
        Long productId,
        BigDecimal amount,
        String description
) {
}
