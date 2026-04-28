package ru.t1.feature7.payments.products;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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
        PaymentProductController controller = new PaymentProductController(paymentProductService, new PaymentProductMapper());
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void returnsProductsByUserId() throws Exception {
        PaymentProduct firstProduct = new PaymentProduct(
                101L,
                "40817810000000000001",
                new BigDecimal("15320.45"),
                "ACCOUNT",
                7L
        );
        PaymentProduct secondProduct = new PaymentProduct(
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
}
