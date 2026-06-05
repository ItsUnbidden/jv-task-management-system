package com.unbidden.jvtaskmanagementsystem.dto.project;

import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.DropboxOperationResult;

import lombok.Data;

@Data
public class DeleteProjectResponseDto {
    private final Long projectId;

    private final DropboxOperationResult dropboxResult;

    public DeleteProjectResponseDto(Long projectId, DropboxOperationResult dropboxResult) {
        this.projectId = projectId;
        this.dropboxResult = dropboxResult;
    }
}
