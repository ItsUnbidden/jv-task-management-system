package com.unbidden.jvtaskmanagementsystem.exception;

public class ExpectedException extends Exception {
    public ExpectedException(String message) {
        super(message);
    }

    public ExpectedException(String message, Throwable cause) {
        super(message, cause);
    }
}
