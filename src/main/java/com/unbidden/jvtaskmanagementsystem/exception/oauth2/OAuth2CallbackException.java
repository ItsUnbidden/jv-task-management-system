package com.unbidden.jvtaskmanagementsystem.exception.oauth2;

import com.unbidden.jvtaskmanagementsystem.exception.ErrorType;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OAuth2CallbackException extends OAuth2AuthorizationException {
    private final String error;

    private final String errorDescription;

    public OAuth2CallbackException(String message, String error, String description) {
        super(message, ErrorType.OAUTH2_CALLBACK_FAILURE);
        this.error = error;
        this.errorDescription = description;
    }
}
