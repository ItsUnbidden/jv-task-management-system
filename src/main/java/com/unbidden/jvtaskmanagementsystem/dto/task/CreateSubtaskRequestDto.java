package com.unbidden.jvtaskmanagementsystem.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSubtaskRequestDto(@NotBlank @Size(max = 200) String name, Long taskId) {}
