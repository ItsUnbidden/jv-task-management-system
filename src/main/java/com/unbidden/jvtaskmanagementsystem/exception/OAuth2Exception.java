package com.unbidden.jvtaskmanagementsystem.exception;

public class OAuth2Exception extends RuntimeException {
    public OAuth2Exception(String msg) {
        super(msg);
    }

    public OAuth2Exception(String msg, Throwable cause) {
        super(msg, cause);
    }
}
