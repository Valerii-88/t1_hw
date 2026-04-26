package ru.t1.feature6;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import ru.t1.feature6.products.Product;
import ru.t1.feature6.products.ProductRepository;
import ru.t1.feature6.products.ProductResponse;
import ru.t1.feature6.users.User;
import ru.t1.feature6.users.UserRepository;

import java.util.List;

public final class Feature6Demo {
    private Feature6Demo() {
    }

    public static void main(String[] args) {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(Feature6Application.class)
                .web(WebApplicationType.SERVLET)
                .logStartupInfo(false)
                .properties("server.port=0")
                .run(args)) {
            long demoUserId = resolveDemoUserId(context);
            long demoProductId = resolveDemoProductId(context, demoUserId);
            int port = ((ServletWebServerApplicationContext) context).getWebServer().getPort();

            RestClient restClient = RestClient.builder()
                    .baseUrl("http://127.0.0.1:" + port)
                    .build();

            List<ProductResponse> userProducts = restClient.get()
                    .uri("/api/users/{userId}/products", demoUserId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

            ProductResponse product = restClient.get()
                    .uri("/api/products/{productId}", demoProductId)
                    .retrieve()
                    .body(ProductResponse.class);

            System.out.println("GET /api/users/" + demoUserId + "/products");
            userProducts.forEach(productResponse -> System.out.println("  " + productResponse));

            System.out.println("GET /api/products/" + demoProductId);
            System.out.println("  " + product);
        }
    }

    private static long resolveDemoUserId(ConfigurableApplicationContext context) {
        UserRepository userRepository = context.getBean(UserRepository.class);
        User user = userRepository.findByUsername("test_user_1")
                .orElseThrow(() -> new IllegalStateException("Seed user 'test_user_1' was not found"));
        return user.getId();
    }

    private static long resolveDemoProductId(ConfigurableApplicationContext context, long userId) {
        ProductRepository productRepository = context.getBean(ProductRepository.class);
        Product product = productRepository.findFirstByUserIdOrderByIdAsc(userId)
                .orElseThrow(() -> new IllegalStateException("Seed product for userId=" + userId + " was not found"));
        return product.getId();
    }
}
