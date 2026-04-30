package ru.t1.feature7.payments.payment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import ru.t1.feature7.payments.api.PaymentRequest;
import ru.t1.feature7.payments.api.PaymentResponse;
import ru.t1.feature7.payments.client.ProductClient;
import ru.t1.feature7.payments.client.ProductClientProductResponse;
import ru.t1.feature7.payments.error.DownstreamProductServiceException;
import ru.t1.feature7.payments.error.ProductOwnershipException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    @Mock
    private ProductClient productClient;

    @Mock
    private PaymentRepository paymentRepository;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(productClient, paymentRepository, new PaymentMapper());
    }

    @Test
    void savesPaymentAfterSuccessfulDebit() {
        when(productClient.getProductById(101L)).thenReturn(product(101L, 7L, "500.00"));
        when(productClient.debitProduct(101L, new BigDecimal("250.00"))).thenReturn(product(101L, 7L, "250.00"));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(1L);
            return payment;
        });

        PaymentResponse response = paymentService.execute(
                new PaymentRequest(7L, 101L, new BigDecimal("250.00"), "utility payment")
        );

        assertEquals(1L, response.paymentId());
        assertEquals(7L, response.userId());
        assertEquals(101L, response.productId());
        assertEquals(new BigDecimal("250.00"), response.amount());
        assertEquals("utility payment", response.description());
        assertNotNull(response.createdAt());
    }

    @Test
    void throwsWhenProductBelongsToAnotherUser() {
        when(productClient.getProductById(101L)).thenReturn(product(101L, 9L, "500.00"));

        ProductOwnershipException exception = assertThrows(
                ProductOwnershipException.class,
                () -> paymentService.execute(new PaymentRequest(7L, 101L, new BigDecimal("250.00"), "utility payment"))
        );

        assertEquals("Product with id=101 does not belong to user with id=7", exception.getMessage());
        verify(productClient, never()).debitProduct(any(), any());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void doesNotSavePaymentWhenDebitFails() {
        when(productClient.getProductById(101L)).thenReturn(product(101L, 7L, "500.00"));
        when(productClient.debitProduct(101L, new BigDecimal("700.00"))).thenThrow(
                new DownstreamProductServiceException(HttpStatus.UNPROCESSABLE_ENTITY, "Product with id=101 has insufficient funds")
        );

        DownstreamProductServiceException exception = assertThrows(
                DownstreamProductServiceException.class,
                () -> paymentService.execute(new PaymentRequest(7L, 101L, new BigDecimal("700.00"), "utility payment"))
        );

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, exception.getStatus());
        assertEquals("Product with id=101 has insufficient funds", exception.getMessage());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void rejectsInvalidAmountPrecision() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.execute(new PaymentRequest(7L, 101L, new BigDecimal("1.001"), "utility payment"))
        );

        assertEquals("Amount must have at most 2 decimal places", exception.getMessage());
        verify(productClient, never()).getProductById(any());
        verify(paymentRepository, never()).save(any());
    }

    private static ProductClientProductResponse product(Long id, Long userId, String balance) {
        return new ProductClientProductResponse(
                id,
                "40817810000000000001",
                new BigDecimal(balance),
                "ACCOUNT",
                userId
        );
    }
}
