package com.unbidden.jvtaskmanagementsystem.exception;

public class InconsistentDataException extends UnexpectedException {
    public InconsistentDataException(String message, ErrorType type, Throwable cause) {
        super(message, type, cause);
    }

    public InconsistentDataException(String message, ErrorType type) {
        super(message, type);
    }
}
