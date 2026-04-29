package ru.t1.feature6.products;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByUserIdOrderByIdAsc(Long userId);

    Optional<Product> findFirstByUserIdOrderByIdAsc(Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Product product
            set product.balance = product.balance - :amount
            where product.id = :productId
              and product.balance >= :amount
            """)
    int debitBalanceIfEnoughFunds(@Param("productId") Long productId, @Param("amount") BigDecimal amount);
}
