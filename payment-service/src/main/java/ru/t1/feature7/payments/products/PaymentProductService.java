package ru.t1.feature7.payments.products;

import org.springframework.stereotype.Service;
import ru.t1.feature7.payments.client.ProductClient;

import java.util.List;

@Service
public class PaymentProductService {
    private final ProductClient productClient;
    private final PaymentProductMapper paymentProductMapper;

    public PaymentProductService(ProductClient productClient, PaymentProductMapper paymentProductMapper) {
        this.productClient = productClient;
        this.paymentProductMapper = paymentProductMapper;
    }

    public List<PaymentProduct> getProductsByUserId(Long userId) {
        return paymentProductMapper.toProducts(productClient.getProductsByUserId(userId));
    }

    public PaymentProduct getProductById(Long productId) {
        return paymentProductMapper.toProduct(productClient.getProductById(productId));
    }
}
