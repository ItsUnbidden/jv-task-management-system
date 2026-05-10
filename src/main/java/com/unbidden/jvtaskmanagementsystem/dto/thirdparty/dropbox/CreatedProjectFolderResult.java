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

    private final CreateProjectFolderErrorTag tag;

    public CreatedProjectFolderResult(@NonNull ThirdPartyOperationStatus status,
            @Nullable CreateProjectFolderErrorTag tag, @Nullable String errorMessage) {
        super(status, errorMessage);
        this.tag = tag;
        this.projectFolderId = null;
        this.projectSharedFolderId = null;
    }

    public CreatedProjectFolderResult(@NonNull ThirdPartyOperationStatus status) {
        super(status);
        this.projectFolderId = null;
        this.projectSharedFolderId = null;
        this.tag = null;
    }

    public CreatedProjectFolderResult(@NonNull ThirdPartyOperationStatus status,
            @Nullable String projectFolderId,
            @Nullable String projectSharedFolderId) {
        super(status);
        this.projectFolderId = projectFolderId;
        this.projectSharedFolderId = projectSharedFolderId;
        this.tag = null;
    }

    public static enum CreateProjectFolderErrorTag {
        UNKNOWN
    }
}
