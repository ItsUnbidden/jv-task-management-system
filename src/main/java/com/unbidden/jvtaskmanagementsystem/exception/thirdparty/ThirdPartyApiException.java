package com.unbidden.jvtaskmanagementsystem.exception.thirdparty;

import com.unbidden.jvtaskmanagementsystem.exception.ErrorType;
import com.unbidden.jvtaskmanagementsystem.exception.UnexpectedException;

public class ThirdPartyApiException extends UnexpectedException {
    public ThirdPartyApiException(String message, ErrorType type) {
        super(message, type);
    }

    public ThirdPartyApiException(String message, ErrorType type, Throwable cause) {
        super(message, type, cause);
    }
}
