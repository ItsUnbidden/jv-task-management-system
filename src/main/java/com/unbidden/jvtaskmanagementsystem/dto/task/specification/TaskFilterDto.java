package com.unbidden.jvtaskmanagementsystem.dto.task.specification;

import java.time.LocalDate;
import java.util.List;

import com.unbidden.jvtaskmanagementsystem.model.Task.TaskPriority;
import com.unbidden.jvtaskmanagementsystem.model.Task.TaskStatus;

import lombok.Data;

@Data
public class TaskFilterDto {
    private Long assigneeId;

    private List<Long> labelIds;

    private LocalDate dueDateFrom;

    private LocalDate dueDateTo;

    private TaskStatus status;

    private TaskPriority priority;
}
