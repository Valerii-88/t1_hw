package ru.t1.feature6.products;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/users/{userId}/products")
    public List<ProductResponse> getProductsByUserId(@PathVariable Long userId) {
        return productService.getAllByUserId(userId).stream()
                .map(ProductResponse::from)
                .toList();
    }

    @GetMapping("/products/{productId}")
    public ProductResponse getProductByProductId(@PathVariable Long productId) {
        return ProductResponse.from(productService.getByProductId(productId));
    }
}
