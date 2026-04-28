package ru.t1.feature7.payments.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import ru.t1.feature7.payments.DownstreamProductServiceException;
import ru.t1.feature7.payments.DownstreamProductServiceUnavailableException;

import java.math.BigDecimal;
import java.util.List;

@Component
public class ProductClient {
    private static final ParameterizedTypeReference<List<ProductClientProductResponse>> PRODUCT_LIST_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final RestTemplate productServiceRestTemplate;
    private final ObjectMapper objectMapper;

    public ProductClient(RestTemplate productServiceRestTemplate, ObjectMapper objectMapper) {
        this.productServiceRestTemplate = productServiceRestTemplate;
        this.objectMapper = objectMapper;
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
        } catch (RestClientResponseException exception) {
            throw translateResponseException(exception);
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
        } catch (RestClientResponseException exception) {
            throw translateResponseException(exception);
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
        } catch (RestClientResponseException exception) {
            throw translateResponseException(exception);
        } catch (ResourceAccessException exception) {
            throw new DownstreamProductServiceUnavailableException("Product service is unavailable");
        }
    }

    private RuntimeException translateResponseException(RestClientResponseException exception) {
        HttpStatusCode statusCode = exception.getStatusCode();
        if (statusCode.is5xxServerError()) {
            return new DownstreamProductServiceUnavailableException("Product service is unavailable");
        }

        if (statusCode instanceof HttpStatus httpStatus && httpStatus.is4xxClientError()) {
            return new DownstreamProductServiceException(httpStatus, extractErrorMessage(exception));
        }

        return new DownstreamProductServiceException(HttpStatus.BAD_REQUEST, extractErrorMessage(exception));
    }

    private String extractErrorMessage(RestClientResponseException exception) {
        String responseBody = exception.getResponseBodyAsString();
        if (responseBody != null && !responseBody.isBlank()) {
            try {
                ProductServiceErrorResponse errorResponse = objectMapper.readValue(responseBody, ProductServiceErrorResponse.class);
                if (errorResponse.error() != null && !errorResponse.error().isBlank()) {
                    return errorResponse.error();
                }
            } catch (JsonProcessingException ignored) {
                // Fall through to a stable generic message when the body is not the expected JSON shape.
            }
        }
        return "Product service request failed";
    }

    private record ProductClientDebitRequest(BigDecimal amount) {
    }
}
