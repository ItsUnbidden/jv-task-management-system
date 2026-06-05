package com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AddUserToProjectFolderResult extends DropboxOperationResult {
    private final Long userId;

    public AddUserToProjectFolderResult(@NonNull ThirdPartyOperationStatus status,
            @Nullable DropboxErrorTag tag, @Nullable String errorMessage) {
        super(status, tag, errorMessage);
        this.userId = null;
    }

    public AddUserToProjectFolderResult(@NonNull ThirdPartyOperationStatus status,
            @Nullable Long userId) {
        super(status);
        this.userId = userId;
    }

    public AddUserToProjectFolderResult(@NonNull ThirdPartyOperationStatus status) {
        super(status);
        this.userId = null;
    }
}
