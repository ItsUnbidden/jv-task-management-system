package com.unbidden.jvtaskmanagementsystem.dto.task;

import java.time.LocalDate;
import java.util.Set;

import com.unbidden.jvtaskmanagementsystem.model.Task.TaskPriority;
import com.unbidden.jvtaskmanagementsystem.model.Task.TaskStatus;

import lombok.Data;

@Data
public class TaskResponseDto {
    private Long id;

    private String name;

    private String description;

    private TaskPriority priority;

    private TaskStatus status;

    private LocalDate dueDate;

    private Long projectId;

    private String projectName;

    private Long assigneeId;

    private String assigneeUsername;

    private Integer amountOfMessages;

    private Integer progress;

    private Set<Long> labelIds;

    private Long version;
}
