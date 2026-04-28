package ru.t1.feature7.payments;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.t1.feature7.payments.api.PaymentRequest;
import ru.t1.feature7.payments.api.PaymentResponse;

@RestController
@RequestMapping("/api/v1")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payments")
    public PaymentResponse createPayment(@RequestBody PaymentRequest request) {
        return paymentService.execute(request);
    }
}
