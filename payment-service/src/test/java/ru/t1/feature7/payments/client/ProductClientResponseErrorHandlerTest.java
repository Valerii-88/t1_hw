package ru.t1.feature7.payments.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.client.MockClientHttpResponse;
import ru.t1.feature7.payments.error.DownstreamProductServiceException;
import ru.t1.feature7.payments.error.DownstreamProductServiceUnavailableException;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductClientResponseErrorHandlerTest {
    private final ProductClientResponseErrorHandler errorHandler =
            new ProductClientResponseErrorHandler(new ObjectMapper());

    @Test
    void mapsNotFoundResponseToDownstreamClientException() throws Exception {
        MockClientHttpResponse response = response(HttpStatus.NOT_FOUND, "{\"error\":\"Product with id=101 was not found\"}");

        DownstreamProductServiceException exception = assertThrows(
                DownstreamProductServiceException.class,
                () -> errorHandler.handleError(response)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Product with id=101 was not found", exception.getMessage());
    }

    @Test
    void preservesUnprocessableEntityStatus() throws Exception {
        MockClientHttpResponse response =
                response(HttpStatus.UNPROCESSABLE_ENTITY, "{\"error\":\"Product with id=101 has insufficient funds\"}");

        DownstreamProductServiceException exception = assertThrows(
                DownstreamProductServiceException.class,
                () -> errorHandler.handleError(response)
        );

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, exception.getStatus());
        assertEquals("Product with id=101 has insufficient funds", exception.getMessage());
    }

    @Test
    void mapsServerErrorsToUnavailableException() {
        MockClientHttpResponse response = response(HttpStatus.INTERNAL_SERVER_ERROR, "{\"error\":\"boom\"}");

        DownstreamProductServiceUnavailableException exception = assertThrows(
                DownstreamProductServiceUnavailableException.class,
                () -> errorHandler.handleError(response)
        );

        assertEquals("Product service is unavailable", exception.getMessage());
    }

    @Test
    void fallsBackToGenericMessageForUnexpectedBody() throws Exception {
        MockClientHttpResponse response = response(HttpStatus.BAD_REQUEST, "not-json");

        DownstreamProductServiceException exception = assertThrows(
                DownstreamProductServiceException.class,
                () -> errorHandler.handleError(response)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Product service request failed", exception.getMessage());
    }

    private static MockClientHttpResponse response(HttpStatus status, String body) {
        return new MockClientHttpResponse(body.getBytes(StandardCharsets.UTF_8), status);
    }
}
