package com.unbidden.jvtaskmanagementsystem.dto.project;

import com.unbidden.jvtaskmanagementsystem.dto.projectrole.ProjectRoleDto;
import com.unbidden.jvtaskmanagementsystem.model.Project.ProjectStatus;
import java.time.LocalDate;
import java.util.Set;
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

    private boolean isPrivate;

    private boolean isDropboxConnected;

    private boolean isCalendarConnected;
}
