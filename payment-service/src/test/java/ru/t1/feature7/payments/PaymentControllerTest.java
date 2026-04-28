package ru.t1.feature7.payments;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.t1.feature7.payments.api.PaymentRequest;
import ru.t1.feature7.payments.api.PaymentResponse;
import ru.t1.feature7.payments.error.DownstreamProductServiceException;
import ru.t1.feature7.payments.error.DownstreamProductServiceUnavailableException;
import ru.t1.feature7.payments.error.PaymentServiceExceptionHandler;
import ru.t1.feature7.payments.error.ProductOwnershipException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {
    @Mock
    private PaymentService paymentService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new PaymentController(paymentService))
                .setControllerAdvice(new PaymentServiceExceptionHandler())
                .build();
    }

    @Test
    void createsPaymentByHttp() throws Exception {
        when(paymentService.execute(any(PaymentRequest.class))).thenReturn(
                new PaymentResponse(
                        1L,
                        7L,
                        101L,
                        new BigDecimal("250.00"),
                        "utility payment",
                        LocalDateTime.parse("2026-04-26T10:15:30")
                )
        );

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":7,"productId":101,"amount":250.00,"description":"utility payment"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(1))
                .andExpect(jsonPath("$.productId").value(101))
                .andExpect(jsonPath("$.amount").value(250.00));
    }

    @Test
    void returnsBadRequestWhenRequestIsInvalid() throws Exception {
        when(paymentService.execute(any(PaymentRequest.class)))
                .thenThrow(new IllegalArgumentException("Amount must be positive"));

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":7,"productId":101,"amount":0,"description":"utility payment"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Amount must be positive"));
    }

    @Test
    void returnsConflictWhenProductBelongsToAnotherUser() throws Exception {
        when(paymentService.execute(any(PaymentRequest.class)))
                .thenThrow(new ProductOwnershipException("Product with id=101 does not belong to user with id=7"));

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":7,"productId":101,"amount":250.00,"description":"utility payment"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Product with id=101 does not belong to user with id=7"));
    }

    @Test
    void returnsNotFoundWhenProductServiceReportsMissingProduct() throws Exception {
        when(paymentService.execute(any(PaymentRequest.class)))
                .thenThrow(new DownstreamProductServiceException(HttpStatus.NOT_FOUND, "Product with id=101 was not found"));

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":7,"productId":101,"amount":250.00,"description":"utility payment"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Product with id=101 was not found"));
    }

    @Test
    void returnsUnprocessableEntityWhenProductServiceReportsInsufficientFunds() throws Exception {
        when(paymentService.execute(any(PaymentRequest.class)))
                .thenThrow(new DownstreamProductServiceException(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "Product with id=101 has insufficient funds"
                ));

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":7,"productId":101,"amount":700.00,"description":"utility payment"}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Product with id=101 has insufficient funds"));
    }

    @Test
    void returnsBadGatewayWhenProductServiceIsUnavailable() throws Exception {
        when(paymentService.execute(any(PaymentRequest.class)))
                .thenThrow(new DownstreamProductServiceUnavailableException("Product service is unavailable"));

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":7,"productId":101,"amount":250.00,"description":"utility payment"}
                                """))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error").value("Product service is unavailable"));
    }
}
