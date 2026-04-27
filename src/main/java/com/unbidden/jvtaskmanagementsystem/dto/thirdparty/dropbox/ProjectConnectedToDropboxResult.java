package com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox;

import java.util.Map;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectConnectedToDropboxResult extends DropboxOperationResult {
    private final CreatedProjectFolderResult projectFolderResult;

    private final Map<Long, CreatedTaskFolderResult> taskFolderResults;

    private final Map<Long, AddUserToProjectFolderResult> userConnectionResults;

    private final ProjectConnectedToDropboxErrorTag tag;

    public ProjectConnectedToDropboxResult(@NonNull ThirdPartyOperationStatus status,
            @Nullable ProjectConnectedToDropboxErrorTag tag, @Nullable String errorMessage) {
        super(status, errorMessage);
        this.projectFolderResult = null;
        this.taskFolderResults = null;
        this.userConnectionResults = null;
        this.tag = tag;
    }

    public ProjectConnectedToDropboxResult(@NonNull ThirdPartyOperationStatus status) {
        super(status);
        this.projectFolderResult = null;
        this.taskFolderResults = null;
        this.userConnectionResults = null;
        this.tag = null;
    }

    public ProjectConnectedToDropboxResult(@NonNull ThirdPartyOperationStatus status,
            @Nullable CreatedProjectFolderResult projectFolderResult,
            @Nullable Map<Long, CreatedTaskFolderResult> taskFolderResults,
            @Nullable Map<Long, AddUserToProjectFolderResult> userConnectionResults) {
        super(status);
        this.projectFolderResult = projectFolderResult;
        this.taskFolderResults = taskFolderResults;
        this.userConnectionResults = userConnectionResults;
        this.tag = null;
    }

    public static enum ProjectConnectedToDropboxErrorTag {
        UNKNOWN
    }
}
