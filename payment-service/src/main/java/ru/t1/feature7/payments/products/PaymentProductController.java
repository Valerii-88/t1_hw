package ru.t1.feature7.payments.products;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class PaymentProductController {
    private final PaymentProductService paymentProductService;
    private final PaymentProductMapper paymentProductMapper;

    public PaymentProductController(PaymentProductService paymentProductService, PaymentProductMapper paymentProductMapper) {
        this.paymentProductService = paymentProductService;
        this.paymentProductMapper = paymentProductMapper;
    }

    @GetMapping("/users/{userId}/products")
    public List<PaymentProductResponse> getProductsByUserId(@PathVariable Long userId) {
        return paymentProductMapper.toResponses(paymentProductService.getProductsByUserId(userId));
    }
}
