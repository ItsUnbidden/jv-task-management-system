package com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CreatedTaskFolderResult extends DropboxOperationResult {
    private final String taskFolderId;

    private final CreateTaskFolderErrorTag tag;

    public CreatedTaskFolderResult(@NonNull ThirdPartyOperationStatus status,
            @Nullable CreateTaskFolderErrorTag tag, @Nullable String errorMessage) {
        super(status, errorMessage);
        this.tag = tag;
        this.taskFolderId = null;
    }

    public CreatedTaskFolderResult(@NonNull ThirdPartyOperationStatus status) {
        super(status);
        this.taskFolderId = null;
        this.tag = null;
    }

    public CreatedTaskFolderResult(@NonNull ThirdPartyOperationStatus status,
            @Nullable String taskFolderId) {
        super(status);
        this.taskFolderId = taskFolderId;
        this.tag = null;
    }

    public static enum CreateTaskFolderErrorTag {
        UNKNOWN
    }
}
