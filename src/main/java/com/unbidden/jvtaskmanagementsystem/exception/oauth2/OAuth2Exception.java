package com.unbidden.jvtaskmanagementsystem.exception.oauth2;

import com.unbidden.jvtaskmanagementsystem.exception.ExpectedException;

public abstract class OAuth2Exception extends ExpectedException {
    public OAuth2Exception(String msg) {
        super(msg);
    }

    public OAuth2Exception(String msg, Throwable cause) {
        super(msg, cause);
    }
}
