package ru.t1.feature6.products;

import org.junit.jupiter.api.Test;
import ru.t1.feature6.users.User;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductMapperTest {
    private final ProductMapper productMapper = new ProductMapper();

    @Test
    void mapsProductToResponse() {
        User user = new User(7L, "test_user_1");
        Product product = new Product(101L, "40817810000000000001", new BigDecimal("15320.45"), ProductType.ACCOUNT, user);

        ProductResponse response = productMapper.toResponse(product);

        assertEquals(new ProductResponse(101L, "40817810000000000001", new BigDecimal("15320.45"), ProductType.ACCOUNT, 7L), response);
    }

    @Test
    void mapsProductListToResponses() {
        User user = new User(7L, "test_user_1");
        List<Product> products = List.of(
                new Product(101L, "40817810000000000001", new BigDecimal("15320.45"), ProductType.ACCOUNT, user),
                new Product(102L, "40817810000000000002", new BigDecimal("810.00"), ProductType.CARD, user)
        );

        List<ProductResponse> responses = productMapper.toResponses(products);

        assertEquals(
                List.of(
                        new ProductResponse(101L, "40817810000000000001", new BigDecimal("15320.45"), ProductType.ACCOUNT, 7L),
                        new ProductResponse(102L, "40817810000000000002", new BigDecimal("810.00"), ProductType.CARD, 7L)
                ),
                responses
        );
    }
}
