package com.unbidden.jvtaskmanagementsystem.dto.task;

import java.time.LocalDate;
import java.util.Set;

import com.unbidden.jvtaskmanagementsystem.model.Task.TaskPriority;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateTaskRequestDto {
    @NotBlank
    @Size(min = 3, max = 50)
    private String name;

    private String description;

    @NotNull
    private TaskPriority priority;

    private LocalDate dueDate;

    private Long newAssigneeId;

    private Set<Long> labelIds;
}
