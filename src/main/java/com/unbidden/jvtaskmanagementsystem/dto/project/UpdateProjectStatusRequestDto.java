package com.unbidden.jvtaskmanagementsystem.dto.project;

import com.unbidden.jvtaskmanagementsystem.model.Project.ProjectStatus;
import com.unbidden.jvtaskmanagementsystem.validation.AllowedValues;
import lombok.Data;

@Data
public class UpdateProjectStatusRequestDto {
    @AllowedValues({"IN_PROGRESS", "COMPLETED"})
    private ProjectStatus newStatus;
}
