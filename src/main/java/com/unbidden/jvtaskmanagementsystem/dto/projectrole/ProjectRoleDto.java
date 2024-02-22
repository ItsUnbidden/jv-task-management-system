package com.unbidden.jvtaskmanagementsystem.dto.projectrole;

import com.unbidden.jvtaskmanagementsystem.model.ProjectRole.ProjectRoleType;
import lombok.Data;

@Data
public class ProjectRoleDto {
    private String username;

    private ProjectRoleType roleType;
}
