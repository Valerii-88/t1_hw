package ru.t1.feature7.payments.products;

import org.springframework.stereotype.Component;
import ru.t1.feature7.payments.client.ProductClientProductResponse;

import java.util.List;

@Component
public class PaymentProductMapper {
    public PaymentProduct toProduct(ProductClientProductResponse response) {
        return new PaymentProduct(
                response.id(),
                response.accountNumber(),
                response.balance(),
                response.productType(),
                response.userId()
        );
    }

    public List<PaymentProduct> toProducts(List<ProductClientProductResponse> responses) {
        return responses.stream()
                .map(this::toProduct)
                .toList();
    }

    public PaymentProductResponse toResponse(PaymentProduct product) {
        return new PaymentProductResponse(
                product.id(),
                product.accountNumber(),
                product.balance(),
                product.productType(),
                product.userId()
        );
    }

    public List<PaymentProductResponse> toResponses(List<PaymentProduct> products) {
        return products.stream()
                .map(this::toResponse)
                .toList();
    }
}
