package ru.t1.feature1.testrunner;

public class Test {
    private final TestResult result;
    private final String name;
    private final Throwable throwable;

    public Test(TestResult result, String name, Throwable throwable) {
        this.result = result;
        this.name = name;
        this.throwable = throwable;
    }

    public TestResult getResult() {
        return result;
    }

    public String getName() {
        return name;
    }

    public Throwable getThrowable() {
        return throwable;
    }
}

