package com.unbidden.jvtaskmanagementsystem.exception.oauth2;

public abstract class OAuth2Exception extends RuntimeException {
    public OAuth2Exception(String msg) {
        super(msg);
    }

    public OAuth2Exception(String msg, Throwable cause) {
        super(msg, cause);
    }
}
