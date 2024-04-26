package com.unbidden.jvtaskmanagementsystem.exception;

public class OAuth2AuthorizationException extends OAuth2Exception {
    public OAuth2AuthorizationException(String message) {
        super(message);
    }

    public OAuth2AuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
