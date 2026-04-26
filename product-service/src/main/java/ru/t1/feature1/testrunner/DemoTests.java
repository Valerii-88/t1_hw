package ru.t1.feature1.testrunner;

import ru.t1.feature1.testrunner.annotations.AfterEach;
import ru.t1.feature1.testrunner.annotations.BeforeEach;
import ru.t1.feature1.testrunner.annotations.Disabled;
import ru.t1.feature1.testrunner.annotations.Order;

public class DemoTests {
    private int counter;

    @BeforeEach
    void beforeEach() {
        counter++;
    }

    @AfterEach
    void afterEach() {
        counter++;
    }

    @ru.t1.feature1.testrunner.annotations.Test("successfull test")
    @Order(1)
    void success() {
        if (counter <= 0) {
            throw new IllegalStateException("Counter was not initialized");
        }
    }

    @ru.t1.feature1.testrunner.annotations.Test("failed assertion")
    @Order(2)
    void failed() {
        throw new TestAssertionError("Expected value to be equal");
    }

    @ru.t1.feature1.testrunner.annotations.Test("unexpected exception")
    @Order(3)
    void error() {
        throw new RuntimeException("Unexpected error");
    }

    @ru.t1.feature1.testrunner.annotations.Test("disabled test")
    @Disabled
    void skipped() {
    }
}

