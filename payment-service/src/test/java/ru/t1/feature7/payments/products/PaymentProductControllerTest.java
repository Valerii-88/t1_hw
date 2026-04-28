package ru.t1.feature7.payments.products;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.t1.feature7.payments.error.DownstreamProductServiceException;
import ru.t1.feature7.payments.error.DownstreamProductServiceUnavailableException;
import ru.t1.feature7.payments.error.PaymentServiceExceptionHandler;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PaymentProductControllerTest {
    @Mock
    private PaymentProductService paymentProductService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        PaymentProductController controller = new PaymentProductController(paymentProductService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new PaymentServiceExceptionHandler())
                .build();
    }

    @Test
    void returnsProductsByUserId() throws Exception {
        PaymentProductResponse firstProduct = new PaymentProductResponse(
                101L,
                "40817810000000000001",
                new BigDecimal("15320.45"),
                "ACCOUNT",
                7L
        );
        PaymentProductResponse secondProduct = new PaymentProductResponse(
                102L,
                "40817810000000000002",
                new BigDecimal("810.00"),
                "CARD",
                7L
        );

        when(paymentProductService.getProductsByUserId(7L)).thenReturn(List.of(firstProduct, secondProduct));

        mockMvc.perform(get("/api/v1/users/7/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(101))
                .andExpect(jsonPath("$[0].accountNumber").value("40817810000000000001"))
                .andExpect(jsonPath("$[0].balance").value(15320.45))
                .andExpect(jsonPath("$[0].productType").value("ACCOUNT"))
                .andExpect(jsonPath("$[0].userId").value(7))
                .andExpect(jsonPath("$[1].id").value(102))
                .andExpect(jsonPath("$[1].productType").value("CARD"));
    }

    @Test
    void returnsNotFoundForDownstreamNotFound() throws Exception {
        when(paymentProductService.getProductsByUserId(7L))
                .thenThrow(new DownstreamProductServiceException(HttpStatus.NOT_FOUND, "User with id=7 was not found"));

        mockMvc.perform(get("/api/v1/users/7/products"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User with id=7 was not found"));
    }

    @Test
    void returnsBadRequestForDownstreamClientError() throws Exception {
        when(paymentProductService.getProductsByUserId(0L))
                .thenThrow(new DownstreamProductServiceException(HttpStatus.BAD_REQUEST, "User id must be positive"));

        mockMvc.perform(get("/api/v1/users/0/products"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("User id must be positive"));
    }

    @Test
    void returnsUnprocessableEntityForDownstreamBusinessError() throws Exception {
        when(paymentProductService.getProductsByUserId(7L))
                .thenThrow(new DownstreamProductServiceException(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "Product with id=101 has insufficient funds"
                ));

        mockMvc.perform(get("/api/v1/users/7/products"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Product with id=101 has insufficient funds"));
    }

    @Test
    void returnsBadGatewayForDownstreamUnavailable() throws Exception {
        when(paymentProductService.getProductsByUserId(7L))
                .thenThrow(new DownstreamProductServiceUnavailableException("Product service is unavailable"));

        mockMvc.perform(get("/api/v1/users/7/products"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error").value("Product service is unavailable"));
    }
}
