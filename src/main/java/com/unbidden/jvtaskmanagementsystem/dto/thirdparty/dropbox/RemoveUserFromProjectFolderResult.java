package com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RemoveUserFromProjectFolderResult extends DropboxOperationResult {
    private final RemoveUserFromProjectFolderErrorTag tag;

    public RemoveUserFromProjectFolderResult(@NonNull ThirdPartyOperationStatus status,
            @Nullable RemoveUserFromProjectFolderErrorTag tag, @Nullable String errorMessage) {
        super(status, errorMessage);
        this.tag = tag;
    }

    public RemoveUserFromProjectFolderResult(@NonNull ThirdPartyOperationStatus status) {
        super(status);
        this.tag = null;
    }

    public static enum RemoveUserFromProjectFolderErrorTag {
        UNKNOWN
    }
}
