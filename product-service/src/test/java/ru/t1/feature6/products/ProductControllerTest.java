package ru.t1.feature6.products;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.t1.feature6.users.User;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {
    @Mock
    private ProductService productService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ProductController controller = new ProductController(productService, new ProductMapper());
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new Feature6ExceptionHandler())
                .build();
    }

    @Test
    void returnsProductsByUserId() throws Exception {
        User user = new User(7L, "test_user_1");
        Product firstProduct = new Product(101L, "40817810000000000001", new BigDecimal("15320.45"), ProductType.ACCOUNT, user);
        Product secondProduct = new Product(102L, "40817810000000000002", new BigDecimal("810.00"), ProductType.CARD, user);

        when(productService.getAllByUserId(7L)).thenReturn(List.of(firstProduct, secondProduct));

        mockMvc.perform(get("/api/v1/users/7/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(101))
                .andExpect(jsonPath("$[0].accountNumber").value("40817810000000000001"))
                .andExpect(jsonPath("$[0].productType").value("ACCOUNT"))
                .andExpect(jsonPath("$[0].userId").value(7))
                .andExpect(jsonPath("$[1].productType").value("CARD"));
    }

    @Test
    void returnsNotFoundForMissingProduct() throws Exception {
        when(productService.getByProductId(999L)).thenThrow(new ResourceNotFoundException("Product with id=999 was not found"));

        mockMvc.perform(get("/api/v1/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Product with id=999 was not found"));
    }

    @Test
    void debitsProductByHttp() throws Exception {
        User user = new User(7L, "test_user_1");
        Product product = new Product(101L, "40817810000000000001", new BigDecimal("15000.00"), ProductType.ACCOUNT, user);

        when(productService.debit(101L, new BigDecimal("320.45"))).thenReturn(product);

        mockMvc.perform(post("/api/v1/products/101/debit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":320.45}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(101))
                .andExpect(jsonPath("$.accountNumber").value("40817810000000000001"))
                .andExpect(jsonPath("$.balance").value(15000.00))
                .andExpect(jsonPath("$.productType").value("ACCOUNT"))
                .andExpect(jsonPath("$.userId").value(7));
    }

    @Test
    void returnsUnprocessableEntityForInsufficientFunds() throws Exception {
        when(productService.debit(101L, new BigDecimal("320.45")))
                .thenThrow(new InsufficientFundsException("Product with id=101 has insufficient funds"));

        mockMvc.perform(post("/api/v1/products/101/debit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":320.45}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Product with id=101 has insufficient funds"));
    }
}
