package com.unbidden.jvtaskmanagementsystem.exception.oauth2;

import com.unbidden.jvtaskmanagementsystem.exception.ErrorType;

public class OAuth2PropertiesParsingException extends OAuth2UnexpectedException {
    public OAuth2PropertiesParsingException(String msg, ErrorType type) {
        super(msg, type);
    }
}
