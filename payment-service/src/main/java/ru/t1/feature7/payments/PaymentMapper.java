package ru.t1.feature7.payments;

import org.springframework.stereotype.Component;
import ru.t1.feature7.payments.api.PaymentResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class PaymentMapper {
    public Payment toEntity(Long userId, Long productId, BigDecimal amount, String description, LocalDateTime createdAt) {
        return new Payment(null, userId, productId, amount, description, createdAt);
    }

    public PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getUserId(),
                payment.getProductId(),
                payment.getAmount(),
                payment.getDescription(),
                payment.getCreatedAt()
        );
    }
}
