package com.unbidden.jvtaskmanagementsystem.dto.project.internal;

import java.util.Map;
import java.util.Set;

import org.springframework.lang.NonNull;

import com.unbidden.jvtaskmanagementsystem.dto.task.internal.CreatedTaskFolderResult;

import lombok.Data;

@Data
public class ProjectConnectedToDropboxResult {
    @NonNull
    private CreatedProjectFolderResult projectFolderResult;

    @NonNull
    private Map<Long, CreatedTaskFolderResult> taskFolderResults;

    @NonNull
    private Set<Long> connectedUserIds;
}
