package com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DeleteResult extends DropboxOperationResult {
    private final DeleteErrorTag tag;

    public DeleteResult(@NonNull ThirdPartyOperationStatus status, @Nullable DeleteErrorTag tag, @Nullable String errorMessage) {
        super(status, errorMessage);
        this.tag = tag;
    }

    public DeleteResult(@NonNull ThirdPartyOperationStatus status) {
        super(status);
        this.tag = null;
    }

    public static enum DeleteErrorTag {
        UNKNOWN
    }
}
