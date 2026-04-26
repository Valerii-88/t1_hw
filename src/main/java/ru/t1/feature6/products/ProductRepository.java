package ru.t1.feature6.products;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByUserIdOrderByIdAsc(Long userId);

    Optional<Product> findFirstByUserIdOrderByIdAsc(Long userId);
}
