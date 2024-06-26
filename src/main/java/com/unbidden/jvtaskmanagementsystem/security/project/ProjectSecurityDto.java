package com.unbidden.jvtaskmanagementsystem.security.project;

import com.unbidden.jvtaskmanagementsystem.model.Project;
import com.unbidden.jvtaskmanagementsystem.model.ProjectRole.ProjectRoleType;
import com.unbidden.jvtaskmanagementsystem.model.User;
import lombok.Data;

@Data
public class ProjectSecurityDto {
    private Project project;

    private User user;

    private ProjectRoleType roleRequired;

    private boolean bypassIfPublic;

    public ProjectSecurityDto(Project project, User user, ProjectRoleType roleRequired,
            boolean bypassIfPublic) {
        this.project = project;
        this.user = user;
        this.roleRequired = roleRequired;
        this.bypassIfPublic = bypassIfPublic;
    }
}
