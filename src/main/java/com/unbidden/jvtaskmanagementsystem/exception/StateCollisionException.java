package com.unbidden.jvtaskmanagementsystem.exception;

public class StateCollisionException extends UnexpectedException {
    public StateCollisionException(String message, ErrorType type) {
        super(message, type);
    }
}
