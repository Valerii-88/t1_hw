package ru.t1.feature6.products;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ProductController {
    private final ProductService productService;
    private final ProductMapper productMapper;

    public ProductController(ProductService productService, ProductMapper productMapper) {
        this.productService = productService;
        this.productMapper = productMapper;
    }

    @GetMapping("/users/{userId}/products")
    public List<ProductResponse> getProductsByUserId(@PathVariable Long userId) {
        return productMapper.toResponses(productService.getAllByUserId(userId));
    }

    @GetMapping("/products/{productId}")
    public ProductResponse getProductByProductId(@PathVariable Long productId) {
        return productMapper.toResponse(productService.getByProductId(productId));
    }
}
