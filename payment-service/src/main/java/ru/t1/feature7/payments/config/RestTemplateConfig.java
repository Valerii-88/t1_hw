package ru.t1.feature7.payments.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import ru.t1.feature7.payments.client.ProductClientResponseErrorHandler;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate productServiceRestTemplate(
            RestTemplateBuilder restTemplateBuilder,
            ProductClientResponseErrorHandler productClientResponseErrorHandler,
            @Value("${services.product-service.base-url}") String productServiceBaseUrl
    ) {
        return restTemplateBuilder
                .rootUri(productServiceBaseUrl)
                .errorHandler(productClientResponseErrorHandler)
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(3))
                .build();
    }
}
