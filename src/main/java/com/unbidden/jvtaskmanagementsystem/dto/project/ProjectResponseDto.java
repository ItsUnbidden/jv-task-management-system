package com.unbidden.jvtaskmanagementsystem.dto.project;

import java.time.LocalDate;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.unbidden.jvtaskmanagementsystem.dto.projectrole.ProjectRoleDto;
import com.unbidden.jvtaskmanagementsystem.model.Project.ProjectStatus;

import lombok.Data;

@Data
public class ProjectResponseDto {
    private Long id;

    private String name;

    private String description;

    private LocalDate startDate;

    private LocalDate endDate;

    private ProjectStatus status;
    
    private Set<ProjectRoleDto> projectRoles;

    @JsonProperty("isPrivate")
    private boolean isPrivate;

    @JsonProperty("isDropboxConnected")
    private boolean isDropboxConnected;

    @JsonProperty("isCalendarConnected")
    private boolean isCalendarConnected;
}
