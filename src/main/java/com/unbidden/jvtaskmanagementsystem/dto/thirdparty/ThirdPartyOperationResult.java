package com.unbidden.jvtaskmanagementsystem.dto.thirdparty;

import org.springframework.lang.NonNull;

import lombok.Data;

@Data
public class ThirdPartyOperationResult {
    @NonNull
    private final ThirdPartyOperationStatus status;

    public static enum ThirdPartyOperationStatus {
        SUCCESS,
        PARTIAL_SUCCESS,
        SKIPPED,
        TOKEN_REJECTED,
        RAN_OUT_OF_RETRIES,
        RETRY_TOO_LONG,
        NOT_APPLICABLE,
        FAILED
    }
}
