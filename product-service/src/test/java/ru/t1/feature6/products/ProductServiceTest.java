package ru.t1.feature6.products;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.t1.feature6.users.User;
import ru.t1.feature6.users.UserRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void returnsAllProductsForExistingUser() {
        User user = new User(7L, "test_user_1");
        List<Product> products = List.of(
                new Product(101L, "40817810000000000001", new BigDecimal("15320.45"), ProductType.ACCOUNT, user),
                new Product(102L, "40817810000000000002", new BigDecimal("810.00"), ProductType.CARD, user)
        );

        when(userRepository.existsById(7L)).thenReturn(true);
        when(productRepository.findAllByUserIdOrderByIdAsc(7L)).thenReturn(products);

        assertEquals(products, productService.getAllByUserId(7L));
    }

    @Test
    void throwsWhenUserDoesNotExist() {
        when(userRepository.existsById(77L)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productService.getAllByUserId(77L)
        );

        assertEquals("User with id=77 was not found", exception.getMessage());
    }

    @Test
    void returnsProductById() {
        User user = new User(7L, "test_user_1");
        Product product = new Product(101L, "40817810000000000001", new BigDecimal("15320.45"), ProductType.ACCOUNT, user);

        when(productRepository.findById(101L)).thenReturn(java.util.Optional.of(product));

        assertEquals(product, productService.getByProductId(101L));
    }

    @Test
    void throwsWhenProductIsMissing() {
        when(productRepository.findById(999L)).thenReturn(java.util.Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productService.getByProductId(999L)
        );

        assertEquals("Product with id=999 was not found", exception.getMessage());
    }

    @Test
    void debitsProductBalanceWhenEnoughFundsExist() {
        User user = new User(7L, "test_user_1");
        Product product = new Product(101L, "40817810000000000001", new BigDecimal("15000.00"), ProductType.ACCOUNT, user);

        when(productRepository.debitBalanceIfEnoughFunds(101L, new BigDecimal("320.45"))).thenReturn(1);
        when(productRepository.findById(101L)).thenReturn(Optional.of(product));

        Product debited = productService.debit(101L, new BigDecimal("320.45"));

        assertEquals(new BigDecimal("15000.00"), debited.getBalance());
        verify(productRepository).debitBalanceIfEnoughFunds(101L, new BigDecimal("320.45"));
    }

    @Test
    void throwsWhenBalanceIsInsufficient() {
        when(productRepository.debitBalanceIfEnoughFunds(101L, new BigDecimal("150.00"))).thenReturn(0);
        when(productRepository.existsById(101L)).thenReturn(true);

        InsufficientFundsException exception = assertThrows(
                InsufficientFundsException.class,
                () -> productService.debit(101L, new BigDecimal("150.00"))
        );

        assertEquals("Product with id=101 has insufficient funds", exception.getMessage());
    }

    @Test
    void throwsWhenProductIsMissingDuringDebit() {
        when(productRepository.debitBalanceIfEnoughFunds(999L, new BigDecimal("150.00"))).thenReturn(0);
        when(productRepository.existsById(999L)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productService.debit(999L, new BigDecimal("150.00"))
        );

        assertEquals("Product with id=999 was not found", exception.getMessage());
    }

    @Test
    void throwsWhenDebitAmountIsNotPositive() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.debit(101L, BigDecimal.ZERO)
        );

        assertEquals("Amount must be positive", exception.getMessage());
        verify(productRepository, never()).debitBalanceIfEnoughFunds(101L, BigDecimal.ZERO);
    }

    @Test
    void throwsWhenDebitAmountHasMoreThanTwoDecimalPlaces() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.debit(101L, new BigDecimal("0.001"))
        );

        assertEquals("Amount must have at most 2 decimal places", exception.getMessage());
        verify(productRepository, never()).debitBalanceIfEnoughFunds(101L, new BigDecimal("0.001"));
    }
}
