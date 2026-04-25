package ru.t1.testrunner;

import ru.t1.testrunner.annotations.AfterEach;
import ru.t1.testrunner.annotations.AfterSuite;
import ru.t1.testrunner.annotations.BeforeEach;
import ru.t1.testrunner.annotations.BeforeSuite;
import ru.t1.testrunner.annotations.Disabled;
import ru.t1.testrunner.annotations.Order;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class TestRunner {
    private TestRunner() {
    }

    public static Map<TestResult, List<Test>> runTests(Class<?> testClass) {
        Constructor<?> constructor = resolveConstructor(testClass);
        TestMetadata metadata = collectMetadata(testClass);
        Map<TestResult, List<Test>> results = initResults();

        Throwable beforeSuiteFailure = invokeSuiteMethods(metadata.beforeSuiteMethods(), testClass);
        if (beforeSuiteFailure != null) {
            for (TestCase testCase : metadata.testCases()) {
                TestResult result = testCase.disabled() ? TestResult.SKIPPED : TestResult.ERROR;
                Throwable throwable = testCase.disabled() ? null : beforeSuiteFailure;
                addResult(results, new Test(result, testCase.name(), throwable));
            }
            invokeSuiteMethods(metadata.afterSuiteMethods(), testClass);
            return results;
        }

        for (TestCase testCase : metadata.testCases()) {
            if (testCase.disabled()) {
                addResult(results, new Test(TestResult.SKIPPED, testCase.name(), null));
                continue;
            }

            Object instance = instantiate(testClass, constructor);
            Test result = executeTestCase(instance, testCase, metadata.beforeEachMethods(), metadata.afterEachMethods());
            addResult(results, result);
        }

        invokeSuiteMethods(metadata.afterSuiteMethods(), testClass);
        return results;
    }

    private static Test executeTestCase(
            Object instance,
            TestCase testCase,
            List<Method> beforeEachMethods,
            List<Method> afterEachMethods
    ) {
        Throwable beforeEachFailure = invokeInstanceMethods(beforeEachMethods, instance);
        TestResult result;
        Throwable throwable;

        if (beforeEachFailure != null) {
            result = TestResult.ERROR;
            throwable = beforeEachFailure;
        } else {
            try {
                testCase.method().invoke(instance);
                result = TestResult.SUCCESS;
                throwable = null;
            } catch (InvocationTargetException exception) {
                throwable = exception.getCause();
                result = throwable instanceof TestAssertionError ? TestResult.FAILED : TestResult.ERROR;
            } catch (ReflectiveOperationException exception) {
                result = TestResult.ERROR;
                throwable = exception;
            }
        }

        Throwable afterEachFailure = invokeInstanceMethods(afterEachMethods, instance);
        if (afterEachFailure != null) {
            result = TestResult.ERROR;
            throwable = afterEachFailure;
        }

        return new Test(result, testCase.name(), throwable);
    }

    private static Map<TestResult, List<Test>> initResults() {
        Map<TestResult, List<Test>> results = new EnumMap<>(TestResult.class);
        for (TestResult value : TestResult.values()) {
            results.put(value, new ArrayList<>());
        }
        return results;
    }

    private static void addResult(Map<TestResult, List<Test>> results, Test test) {
        results.get(test.getResult()).add(test);
    }

    private static Constructor<?> resolveConstructor(Class<?> testClass) {
        try {
            Constructor<?> constructor = testClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor;
        } catch (NoSuchMethodException exception) {
            throw new BadTestClassError("Test class must declare a no-args constructor: " + testClass.getName(), exception);
        }
    }

    private static Object instantiate(Class<?> testClass, Constructor<?> constructor) {
        try {
            return constructor.newInstance();
        } catch (ReflectiveOperationException exception) {
            throw new BadTestClassError("Cannot create test class instance: " + testClass.getName(), exception);
        }
    }

    private static TestMetadata collectMetadata(Class<?> testClass) {
        List<Method> beforeSuiteMethods = new ArrayList<>();
        List<Method> afterSuiteMethods = new ArrayList<>();
        List<Method> beforeEachMethods = new ArrayList<>();
        List<Method> afterEachMethods = new ArrayList<>();
        List<TestCase> testCases = new ArrayList<>();

        for (Method method : testClass.getDeclaredMethods()) {
            method.setAccessible(true);
            validateMethod(testClass, method);

            if (method.isAnnotationPresent(BeforeSuite.class)) {
                beforeSuiteMethods.add(method);
            }
            if (method.isAnnotationPresent(AfterSuite.class)) {
                afterSuiteMethods.add(method);
            }
            if (method.isAnnotationPresent(BeforeEach.class)) {
                beforeEachMethods.add(method);
            }
            if (method.isAnnotationPresent(AfterEach.class)) {
                afterEachMethods.add(method);
            }
            if (method.isAnnotationPresent(ru.t1.testrunner.annotations.Test.class)) {
                testCases.add(toTestCase(method));
            }
        }

        Comparator<Method> lifecycleComparator = Comparator.comparing(Method::getName);
        beforeSuiteMethods.sort(lifecycleComparator);
        afterSuiteMethods.sort(lifecycleComparator);
        beforeEachMethods.sort(lifecycleComparator);
        afterEachMethods.sort(lifecycleComparator);
        testCases.sort(testCaseComparator());

        return new TestMetadata(beforeSuiteMethods, afterSuiteMethods, beforeEachMethods, afterEachMethods, testCases);
    }

    private static void validateMethod(Class<?> testClass, Method method) {
        boolean isStatic = Modifier.isStatic(method.getModifiers());
        if (isStatic && (method.isAnnotationPresent(ru.t1.testrunner.annotations.Test.class)
                || method.isAnnotationPresent(BeforeEach.class)
                || method.isAnnotationPresent(AfterEach.class))) {
            throw new BadTestClassError("Method must not be static: " + testClass.getName() + "#" + method.getName());
        }
        if (!isStatic && (method.isAnnotationPresent(BeforeSuite.class) || method.isAnnotationPresent(AfterSuite.class))) {
            throw new BadTestClassError("Suite method must be static: " + testClass.getName() + "#" + method.getName());
        }
    }

    private static TestCase toTestCase(Method method) {
        ru.t1.testrunner.annotations.Test testAnnotation =
                method.getAnnotation(ru.t1.testrunner.annotations.Test.class);
        Order orderAnnotation = method.getAnnotation(Order.class);
        int priority = validatedRange(testAnnotation.priority(), 0, 10, "@Test.priority", method);
        int order = orderAnnotation == null ? 5 : validatedRange(orderAnnotation.value(), 1, 10, "@Order", method);
        String declaredName = testAnnotation.value().isBlank() ? method.getName() : testAnnotation.value();
        boolean disabled = method.isAnnotationPresent(Disabled.class);
        return new TestCase(method, declaredName, priority, order, disabled);
    }

    private static int validatedRange(int value, int min, int max, String label, Method method) {
        if (value < min || value > max) {
            throw new BadTestClassError(label + " must be in range [" + min + ", " + max + "] for method " + method.getName());
        }
        return value;
    }

    private static Comparator<TestCase> testCaseComparator() {
        return Comparator.comparingInt(TestCase::order)
                .thenComparing(Comparator.comparingInt(TestCase::priority).reversed())
                .thenComparing(TestCase::name);
    }

    private static Throwable invokeSuiteMethods(List<Method> methods, Class<?> testClass) {
        for (Method method : methods) {
            try {
                method.invoke(null);
            } catch (InvocationTargetException exception) {
                return exception.getCause();
            } catch (ReflectiveOperationException exception) {
                return new BadTestClassError("Cannot invoke suite method: " + testClass.getName() + "#" + method.getName(), exception);
            }
        }
        return null;
    }

    private static Throwable invokeInstanceMethods(List<Method> methods, Object instance) {
        for (Method method : methods) {
            try {
                method.invoke(instance);
            } catch (InvocationTargetException exception) {
                return exception.getCause();
            } catch (ReflectiveOperationException exception) {
                return exception;
            }
        }
        return null;
    }

    private record TestMetadata(
            List<Method> beforeSuiteMethods,
            List<Method> afterSuiteMethods,
            List<Method> beforeEachMethods,
            List<Method> afterEachMethods,
            List<TestCase> testCases
    ) {
    }

    private record TestCase(Method method, String name, int priority, int order, boolean disabled) {
    }
}
