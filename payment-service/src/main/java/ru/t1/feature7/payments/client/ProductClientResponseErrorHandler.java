package ru.t1.feature7.payments.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.ResponseErrorHandler;
import ru.t1.feature7.payments.error.DownstreamProductServiceException;
import ru.t1.feature7.payments.error.DownstreamProductServiceUnavailableException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class ProductClientResponseErrorHandler implements ResponseErrorHandler {
    private final ObjectMapper objectMapper;

    public ProductClientResponseErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().isError();
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        HttpStatusCode statusCode = response.getStatusCode();
        if (statusCode.is5xxServerError()) {
            throw new DownstreamProductServiceUnavailableException("Product service is unavailable");
        }

        if (statusCode instanceof HttpStatus httpStatus && httpStatus.is4xxClientError()) {
            throw new DownstreamProductServiceException(httpStatus, extractErrorMessage(response));
        }

        throw new DownstreamProductServiceException(HttpStatus.BAD_REQUEST, extractErrorMessage(response));
    }

    private String extractErrorMessage(ClientHttpResponse response) throws IOException {
        String responseBody = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
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
}
