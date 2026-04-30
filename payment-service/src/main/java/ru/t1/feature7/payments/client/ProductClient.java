package ru.t1.feature7.payments.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import ru.t1.feature7.payments.error.DownstreamProductServiceUnavailableException;

import java.math.BigDecimal;
import java.util.List;

@Component
public class ProductClient {
    private static final ParameterizedTypeReference<List<ProductClientProductResponse>> PRODUCT_LIST_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final RestTemplate productServiceRestTemplate;

    public ProductClient(RestTemplate productServiceRestTemplate) {
        this.productServiceRestTemplate = productServiceRestTemplate;
    }

    public List<ProductClientProductResponse> getProductsByUserId(Long userId) {
        try {
            return productServiceRestTemplate.exchange(
                    "/api/v1/users/{userId}/products",
                    HttpMethod.GET,
                    null,
                    PRODUCT_LIST_TYPE,
                    userId
            ).getBody();
        } catch (ResourceAccessException exception) {
            throw new DownstreamProductServiceUnavailableException("Product service is unavailable");
        }
    }

    public ProductClientProductResponse getProductById(Long productId) {
        try {
            return productServiceRestTemplate.getForObject(
                    "/api/v1/products/{productId}",
                    ProductClientProductResponse.class,
                    productId
            );
        } catch (ResourceAccessException exception) {
            throw new DownstreamProductServiceUnavailableException("Product service is unavailable");
        }
    }

    public ProductClientProductResponse debitProduct(Long productId, BigDecimal amount) {
        try {
            return productServiceRestTemplate.exchange(
                    "/api/v1/products/{productId}/debit",
                    HttpMethod.POST,
                    new HttpEntity<>(new ProductClientDebitRequest(amount)),
                    ProductClientProductResponse.class,
                    productId
            ).getBody();
        } catch (ResourceAccessException exception) {
            throw new DownstreamProductServiceUnavailableException("Product service is unavailable");
        }
    }

    private record ProductClientDebitRequest(BigDecimal amount) {
    }
}
