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

    public PaymentProductController(PaymentProductService paymentProductService) {
        this.paymentProductService = paymentProductService;
    }

    @GetMapping("/users/{userId}/products")
    public List<PaymentProductResponse> getProductsByUserId(@PathVariable Long userId) {
        return paymentProductService.getProductsByUserId(userId);
    }
}
