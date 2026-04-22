package com.unbidden.jvtaskmanagementsystem.dto.project;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class RemoveUserFromProjectResponseDto {
    @JsonProperty("isDropboxDisconnected")
    private boolean isDropboxDisconnected;

    @JsonProperty("isCalendarDisconnected")
    private boolean isCalendarDisconnected;
}
