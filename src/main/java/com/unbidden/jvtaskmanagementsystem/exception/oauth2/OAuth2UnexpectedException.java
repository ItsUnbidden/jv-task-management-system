package com.unbidden.jvtaskmanagementsystem.exception.oauth2;

import com.unbidden.jvtaskmanagementsystem.exception.ErrorType;
import com.unbidden.jvtaskmanagementsystem.exception.UnexpectedException;

public class OAuth2UnexpectedException extends UnexpectedException {
    public OAuth2UnexpectedException(String msg, ErrorType type) {
        super(msg, type);
    }

    public OAuth2UnexpectedException(String msg, ErrorType type, Throwable cause) {
        super(msg, type, cause);
    }
}
