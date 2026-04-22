package com.unbidden.jvtaskmanagementsystem.dto.project;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class DeleteProjectResponseDto {
    @JsonProperty("isDropboxFolderDeleted")
    private boolean isDropboxFolderDeleted;

    @JsonProperty("isCalendarDeleted")
    private boolean isCalendarDeleted;
}
