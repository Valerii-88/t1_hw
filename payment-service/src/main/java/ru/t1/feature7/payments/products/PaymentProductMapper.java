package ru.t1.feature7.payments.products;

import org.springframework.stereotype.Component;
import ru.t1.feature7.payments.client.ProductClientProductResponse;

import java.util.List;

@Component
public class PaymentProductMapper {
    public PaymentProductResponse toResponse(ProductClientProductResponse response) {
        return new PaymentProductResponse(
                response.id(),
                response.accountNumber(),
                response.balance(),
                response.productType(),
                response.userId()
        );
    }

    public List<PaymentProductResponse> toResponses(List<ProductClientProductResponse> responses) {
        return responses.stream()
                .map(this::toResponse)
                .toList();
    }
}
