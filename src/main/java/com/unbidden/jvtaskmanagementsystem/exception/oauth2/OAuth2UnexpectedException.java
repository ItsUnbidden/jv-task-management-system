package com.unbidden.jvtaskmanagementsystem.exception.oauth2;

public class OAuth2UnexpectedException extends RuntimeException {
    public OAuth2UnexpectedException(String msg) {
        super(msg);
    }

    public OAuth2UnexpectedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
