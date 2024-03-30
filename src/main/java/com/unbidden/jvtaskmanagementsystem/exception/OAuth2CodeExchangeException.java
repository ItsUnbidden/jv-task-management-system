package com.unbidden.jvtaskmanagementsystem.exception;

public class OAuth2CodeExchangeException extends RuntimeException {
    public OAuth2CodeExchangeException(String message) {
        super(message);
    }

    public OAuth2CodeExchangeException(String message, Throwable e) {
        super(message, e);
    }
}
