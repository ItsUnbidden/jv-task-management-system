package com.unbidden.jvtaskmanagementsystem.dto.projectrole;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.unbidden.jvtaskmanagementsystem.model.ProjectRole.ProjectRoleType;

import lombok.Data;

@Data
public class ProjectRoleDto {
    private Long userId;

    private String username;

    private ProjectRoleType roleType;

    @JsonProperty("isDropboxConnected")
    private boolean isDropboxConnected;

    @JsonProperty("isCalendarConnected")
    private boolean isCalendarConnected;
}
