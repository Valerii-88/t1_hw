package ru.t1.feature7.payments.products;

import java.math.BigDecimal;

public record PaymentProduct(
        Long id,
        String accountNumber,
        BigDecimal balance,
        String productType,
        Long userId
) {
}
