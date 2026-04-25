package ru.t1.testrunner;

public class BadTestClassError extends Error {
    public BadTestClassError(String message) {
        super(message);
    }

    public BadTestClassError(String message, Throwable cause) {
        super(message, cause);
    }
}
