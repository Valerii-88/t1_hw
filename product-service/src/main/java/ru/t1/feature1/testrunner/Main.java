package ru.t1.feature1.testrunner;

import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Map<TestResult, List<Test>> results = TestRunner.runTests(DemoTests.class);

        for (TestResult result : TestResult.values()) {
            List<Test> tests = results.get(result);
            System.out.println(result + ": " + tests.size());
            for (Test test : tests) {
                String line = "  - " + test.getName();
                if (test.getThrowable() != null) {
                    line += " -> " + test.getThrowable().getClass().getSimpleName()
                            + ": " + test.getThrowable().getMessage();
                }
                System.out.println(line);
            }
        }
    }
}

