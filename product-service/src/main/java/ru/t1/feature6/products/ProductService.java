package ru.t1.feature6.products;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.feature6.users.UserRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProductService(ProductRepository productRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public List<Product> getAllByUserId(Long userId) {
        validateId(userId, "User");
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User with id=" + userId + " was not found");
        }
        return productRepository.findAllByUserIdOrderByIdAsc(userId);
    }

    public Product getByProductId(Long productId) {
        validateId(productId, "Product");
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product with id=" + productId + " was not found"));
    }

    private void validateId(Long id, String label) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(label + " id must be positive");
        }
    }
}
