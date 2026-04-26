package ru.t1.feature7.payments.api;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long paymentId,
        Long userId,
        Long productId,
        BigDecimal amount,
        String description,
        LocalDateTime createdAt
) {
}
