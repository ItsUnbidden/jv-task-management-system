package com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AddUserToProjectFolderResult extends DropboxOperationResult {
    private final Long userId;

    private final AddUserToProjectFolderErrorTag tag;

    public AddUserToProjectFolderResult(@NonNull ThirdPartyOperationStatus status,
            @Nullable AddUserToProjectFolderErrorTag tag, @Nullable String errorMessage) {
        super(status, errorMessage);
        this.tag = tag;
        this.userId = null;
    }

    public AddUserToProjectFolderResult(@NonNull ThirdPartyOperationStatus status,
            @Nullable Long userId) {
        super(status);
        this.userId = userId;
        this.tag = null;
    }

    public AddUserToProjectFolderResult(@NonNull ThirdPartyOperationStatus status) {
        super(status);
        this.userId = null;
        this.tag = null;
    }

    public static enum AddUserToProjectFolderErrorTag {
        UNKNOWN
    }
}
