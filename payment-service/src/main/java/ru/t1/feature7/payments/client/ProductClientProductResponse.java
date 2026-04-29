package ru.t1.feature7.payments.client;

import java.math.BigDecimal;

public record ProductClientProductResponse(
        Long id,
        String accountNumber,
        BigDecimal balance,
        String productType,
        Long userId
) {
}
