package ru.t1.testrunner;

import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class TestRunnerTest {
    @org.junit.jupiter.api.Test
    void runTestsExecutesInExpectedOrderAndCollectsStatuses() {
        OrderedScenario.EVENTS.clear();

        Map<TestResult, List<Test>> results = TestRunner.runTests(OrderedScenario.class);

        Assertions.assertEquals(
                List.of("beforeSuite", "beforeEach", "alpha", "afterEach", "beforeEach", "beta", "afterEach", "beforeEach", "afterEach"),
                OrderedScenario.EVENTS
        );
        Assertions.assertEquals(List.of("alpha"), extractNames(results.get(TestResult.SUCCESS)));
        Assertions.assertEquals(List.of("beta"), extractNames(results.get(TestResult.FAILED)));
        Assertions.assertEquals(List.of("gamma"), extractNames(results.get(TestResult.ERROR)));
        Assertions.assertEquals(List.of("disabled"), extractNames(results.get(TestResult.SKIPPED)));
        Assertions.assertTrue(results.get(TestResult.ERROR).get(0).getThrowable() instanceof IllegalStateException);
    }

    @org.junit.jupiter.api.Test
    void runTestsRejectsStaticTestMethods() {
        BadTestClassError error = Assertions.assertThrows(
                BadTestClassError.class,
                () -> TestRunner.runTests(InvalidStaticTest.class)
        );

        Assertions.assertTrue(error.getMessage().contains("must not be static"));
    }

    @org.junit.jupiter.api.Test
    void runTestsRejectsClassWithoutDefaultConstructor() {
        BadTestClassError error = Assertions.assertThrows(
                BadTestClassError.class,
                () -> TestRunner.runTests(NoDefaultConstructor.class)
        );

        Assertions.assertTrue(error.getMessage().contains("no-args constructor"));
    }

    private static List<String> extractNames(List<Test> tests) {
        return tests.stream().map(Test::getName).toList();
    }

    static class OrderedScenario {
        static final List<String> EVENTS = new ArrayList<>();

        @ru.t1.testrunner.annotations.BeforeSuite
        static void beforeSuite() {
            EVENTS.add("beforeSuite");
        }

        @ru.t1.testrunner.annotations.BeforeEach
        void beforeEach() {
            EVENTS.add("beforeEach");
        }

        @ru.t1.testrunner.annotations.AfterEach
        void afterEach() {
            EVENTS.add("afterEach");
        }

        @ru.t1.testrunner.annotations.Test("beta")
        @ru.t1.testrunner.annotations.Order(2)
        void beta() {
            EVENTS.add("beta");
            throw new TestAssertionError("failed");
        }

        @ru.t1.testrunner.annotations.Test("alpha")
        @ru.t1.testrunner.annotations.Order(1)
        void alpha() {
            EVENTS.add("alpha");
        }

        @ru.t1.testrunner.annotations.Test("gamma")
        @ru.t1.testrunner.annotations.Order(3)
        void gamma() {
            throw new IllegalStateException("boom");
        }

        @ru.t1.testrunner.annotations.Test("disabled")
        @ru.t1.testrunner.annotations.Disabled
        void disabled() {
            EVENTS.add("disabled");
        }
    }

    static class InvalidStaticTest {
        @ru.t1.testrunner.annotations.Test
        static void broken() {
        }
    }

    static class NoDefaultConstructor {
        private final String value;

        NoDefaultConstructor(String value) {
            this.value = value;
        }

        @ru.t1.testrunner.annotations.Test
        void test() {
            Assertions.assertNotNull(value);
        }
    }
}
