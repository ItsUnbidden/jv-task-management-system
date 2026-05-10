package com.unbidden.jvtaskmanagementsystem.exception;

public class UnexpectedException extends RuntimeException {
    private final ErrorType type;

    public UnexpectedException(String message, ErrorType type) {
        super(message);
        this.type = type;
    }

    public UnexpectedException(String message, ErrorType type, Throwable cause) {
        super(message, cause);
        this.type = type;
    }

    public ErrorType getType() {
        return type;
    }
}
