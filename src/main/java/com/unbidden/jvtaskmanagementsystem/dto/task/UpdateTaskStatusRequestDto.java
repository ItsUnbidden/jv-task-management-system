package com.unbidden.jvtaskmanagementsystem.dto.task;

import com.unbidden.jvtaskmanagementsystem.model.Task.TaskStatus;
import com.unbidden.jvtaskmanagementsystem.validation.AllowedValues;

import lombok.Data;

@Data
public class UpdateTaskStatusRequestDto {
    @AllowedValues({"NOT_STARTED", "IN_PROGRESS", "COMPLETED"})
    private TaskStatus newStatus;
}
