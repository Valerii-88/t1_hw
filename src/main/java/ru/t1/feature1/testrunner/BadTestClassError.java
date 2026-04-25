package ru.t1.feature1.testrunner;

public class BadTestClassError extends Error {
    public BadTestClassError(String message) {
        super(message);
    }

    public BadTestClassError(String message, Throwable cause) {
        super(message, cause);
    }
}

