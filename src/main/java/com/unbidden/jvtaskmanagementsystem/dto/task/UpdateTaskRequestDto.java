package com.unbidden.jvtaskmanagementsystem.dto.task;

import com.unbidden.jvtaskmanagementsystem.model.Task.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Data;

@Data
public class UpdateTaskRequestDto {
    @NotBlank
    @Size(min = 1, max = 50)
    private String name;

    private String description;

    @NotNull
    private TaskPriority priority;

    private LocalDate dueDate;

    private Long newAssigneeId;
}
