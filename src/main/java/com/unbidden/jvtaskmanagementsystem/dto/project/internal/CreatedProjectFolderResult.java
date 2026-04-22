package com.unbidden.jvtaskmanagementsystem.dto.project.internal;

import org.springframework.lang.NonNull;

import lombok.Data;

@Data
public class CreatedProjectFolderResult {
    @NonNull
    private String projectFolderId;

    @NonNull
    private String projectSharedFolderId;
}
