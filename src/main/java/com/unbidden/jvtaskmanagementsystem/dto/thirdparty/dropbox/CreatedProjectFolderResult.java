package com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CreatedProjectFolderResult extends DropboxOperationResult {
    private final String projectFolderId;

    private final String projectSharedFolderId;

    public CreatedProjectFolderResult(@NonNull ThirdPartyOperationStatus status,
            @Nullable DropboxErrorTag tag, @Nullable String errorMessage) {
        super(status, tag, errorMessage);
        this.projectFolderId = null;
        this.projectSharedFolderId = null;
    }

    public CreatedProjectFolderResult(@NonNull ThirdPartyOperationStatus status) {
        super(status);
        this.projectFolderId = null;
        this.projectSharedFolderId = null;
    }

    public CreatedProjectFolderResult(@NonNull ThirdPartyOperationStatus status,
            @Nullable String projectFolderId,
            @Nullable String projectSharedFolderId) {
        super(status);
        this.projectFolderId = projectFolderId;
        this.projectSharedFolderId = projectSharedFolderId;
    }
}
