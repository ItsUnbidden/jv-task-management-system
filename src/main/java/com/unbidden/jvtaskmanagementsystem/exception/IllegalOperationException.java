package com.unbidden.jvtaskmanagementsystem.exception;

public class IllegalOperationException extends UnexpectedException {
    public IllegalOperationException(String message, ErrorType type, Throwable cause) {
        super(message, type, cause);
    }

    public IllegalOperationException(String message, ErrorType type) {
        super(message, type);
    }
}
