package com.unbidden.jvtaskmanagementsystem.exception.thirdparty;

import com.unbidden.jvtaskmanagementsystem.exception.ExpectedException;

public class ThirdPartyExpectedException extends ExpectedException {
    public ThirdPartyExpectedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ThirdPartyExpectedException(String message) {
        super(message);
    }
}
