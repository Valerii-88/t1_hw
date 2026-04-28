package ru.t1.feature7.payments.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate productServiceRestTemplate(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${services.product-service.base-url}") String productServiceBaseUrl
    ) {
        return restTemplateBuilder
                .rootUri(productServiceBaseUrl)
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(3))
                .build();
    }
}
