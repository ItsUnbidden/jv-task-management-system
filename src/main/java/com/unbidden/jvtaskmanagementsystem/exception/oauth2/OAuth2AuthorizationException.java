package com.unbidden.jvtaskmanagementsystem.exception.oauth2;

import com.unbidden.jvtaskmanagementsystem.exception.ErrorType;

public class OAuth2AuthorizationException extends OAuth2UnexpectedException {
    public OAuth2AuthorizationException(String message, ErrorType type) {
        super(message, type);
    }

    public OAuth2AuthorizationException(String message, ErrorType type, Throwable cause) {
        super(message, type, cause);
    }
}
