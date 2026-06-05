package com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CreatedTaskFolderResult extends DropboxOperationResult {
    private final String taskFolderId;

    public CreatedTaskFolderResult(@NonNull ThirdPartyOperationStatus status,
            @Nullable DropboxErrorTag tag, @Nullable String errorMessage) {
        super(status, tag, errorMessage);
        this.taskFolderId = null;
    }

    public CreatedTaskFolderResult(@NonNull ThirdPartyOperationStatus status) {
        super(status);
        this.taskFolderId = null;
    }

    public CreatedTaskFolderResult(@NonNull ThirdPartyOperationStatus status,
            @Nullable String taskFolderId) {
        super(status);
        this.taskFolderId = taskFolderId;
    }
}
