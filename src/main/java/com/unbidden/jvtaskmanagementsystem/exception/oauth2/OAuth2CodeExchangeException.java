package com.unbidden.jvtaskmanagementsystem.exception.oauth2;

public class OAuth2CodeExchangeException extends OAuth2Exception {
    public OAuth2CodeExchangeException(String message) {
        super(message);
    }

    public OAuth2CodeExchangeException(String message, Throwable e) {
        super(message, e);
    }
}
