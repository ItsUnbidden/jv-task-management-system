package com.unbidden.jvtaskmanagementsystem.exception.thirdparty;

import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.ThirdPartyOperationResult;
import com.unbidden.jvtaskmanagementsystem.exception.ErrorType;
import com.unbidden.jvtaskmanagementsystem.exception.UnexpectedException;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ThirdPartyApiException extends UnexpectedException {
    private final ThirdPartyOperationResult result;

    public ThirdPartyApiException(String message, ErrorType type, ThirdPartyOperationResult result) {
        super(message, type);
        this.result = result;
    }

    public ThirdPartyApiException(String message, ErrorType type, Throwable cause, ThirdPartyOperationResult result) {
        super(message, type, cause);
        this.result = result;
    }
}
