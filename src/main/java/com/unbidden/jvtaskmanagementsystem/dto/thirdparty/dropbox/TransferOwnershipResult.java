package com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TransferOwnershipResult extends DropboxOperationResult {
    public TransferOwnershipResult(@NonNull ThirdPartyOperationStatus status,
            @Nullable DropboxErrorTag tag, @Nullable String errorMessage) {
        super(status, tag, errorMessage);
    }

    public TransferOwnershipResult(@NonNull ThirdPartyOperationStatus status) {
        super(status);
    }
}
