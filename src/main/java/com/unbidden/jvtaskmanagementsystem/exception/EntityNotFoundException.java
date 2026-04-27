package com.unbidden.jvtaskmanagementsystem.exception;

public class EntityNotFoundException extends UnexpectedException {
    public EntityNotFoundException(String message, ErrorType type) {
        super(message, type);
    }

    public EntityNotFoundException(String message, ErrorType type, Throwable cause) {
        super(message, type, cause);
    }
}
