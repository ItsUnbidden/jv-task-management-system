package com.unbidden.jvtaskmanagementsystem.dto.task.internal;

import org.springframework.lang.NonNull;

import lombok.Data;

@Data
public class CreatedTaskFolderResult {
    @NonNull
    private String taskFolderId;
}
