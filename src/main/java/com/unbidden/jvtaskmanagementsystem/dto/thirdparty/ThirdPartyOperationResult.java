package com.unbidden.jvtaskmanagementsystem.dto.thirdparty;

import org.springframework.lang.NonNull;

import lombok.Data;

@Data
public class ThirdPartyOperationResult {
    @NonNull
    private ThirdPartyOperationStatus status;

    public static enum ThirdPartyOperationStatus {
        SUCCESS,
        SKIPPED,
        NOT_APPLICABLE,
        FAILED
    }
}
