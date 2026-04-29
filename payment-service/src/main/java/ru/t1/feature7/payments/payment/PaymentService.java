package ru.t1.feature7.payments.payment;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.feature7.payments.api.PaymentRequest;
import ru.t1.feature7.payments.api.PaymentResponse;
import ru.t1.feature7.payments.client.ProductClient;
import ru.t1.feature7.payments.client.ProductClientProductResponse;
import ru.t1.feature7.payments.error.ProductOwnershipException;

import java.time.LocalDateTime;

@Service
public class PaymentService {
    private final ProductClient productClient;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    public PaymentService(
            ProductClient productClient,
            PaymentRepository paymentRepository,
            PaymentMapper paymentMapper
    ) {
        this.productClient = productClient;
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
    }

    @Transactional
    public PaymentResponse execute(PaymentRequest request) {
        validateRequest(request);

        ProductClientProductResponse product = productClient.getProductById(request.productId());
        if (!product.userId().equals(request.userId())) {
            throw new ProductOwnershipException(
                    "Product with id=" + request.productId() + " does not belong to user with id=" + request.userId()
            );
        }

        productClient.debitProduct(request.productId(), request.amount());

        Payment payment = paymentRepository.save(
                paymentMapper.toEntity(
                        request.userId(),
                        request.productId(),
                        request.amount(),
                        request.description().trim(),
                        LocalDateTime.now()
                )
        );

        return paymentMapper.toResponse(payment);
    }

    private void validateRequest(PaymentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        if (request.userId() == null || request.userId() <= 0) {
            throw new IllegalArgumentException("User id must be positive");
        }
        if (request.productId() == null || request.productId() <= 0) {
            throw new IllegalArgumentException("Product id must be positive");
        }
        if (request.amount() == null || request.amount().signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (request.amount().scale() > 2) {
            throw new IllegalArgumentException("Amount must have at most 2 decimal places");
        }
        if (request.description() == null || request.description().isBlank()) {
            throw new IllegalArgumentException("Description must not be blank");
        }
    }
}
