package ru.t1.feature6.products;

import java.math.BigDecimal;

public record ProductDebitResponse(
        Long id,
        String accountNumber,
        BigDecimal balance,
        ProductType productType,
        Long userId
) {
}
