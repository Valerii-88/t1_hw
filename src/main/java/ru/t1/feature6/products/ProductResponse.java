package ru.t1.feature6.products;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String accountNumber,
        BigDecimal balance,
        ProductType productType,
        Long userId
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getAccountNumber(),
                product.getBalance(),
                product.getProductType(),
                product.getUser().getId()
        );
    }
}
