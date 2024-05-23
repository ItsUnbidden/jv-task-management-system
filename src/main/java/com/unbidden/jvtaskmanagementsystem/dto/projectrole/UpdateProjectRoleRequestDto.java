package com.unbidden.jvtaskmanagementsystem.dto.projectrole;

import com.unbidden.jvtaskmanagementsystem.model.ProjectRole.ProjectRoleType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateProjectRoleRequestDto {
    @NotNull
    private ProjectRoleType newRole;
}
