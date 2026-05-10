package com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TransferOwnershipResult extends DropboxOperationResult {
    private final TransferOwnershipErrorTag tag;

    public TransferOwnershipResult(@NonNull ThirdPartyOperationStatus status,
            @Nullable TransferOwnershipErrorTag tag, @Nullable String errorMessage) {
        super(status, errorMessage);
        this.tag = tag;
    }

    public TransferOwnershipResult(@NonNull ThirdPartyOperationStatus status) {
        super(status);
        this.tag = null;
    }

    public static enum TransferOwnershipErrorTag {
        UNKNOWN
    }
}
