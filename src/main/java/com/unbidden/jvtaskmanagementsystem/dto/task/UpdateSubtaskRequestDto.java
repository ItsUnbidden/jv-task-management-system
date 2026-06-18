package com.unbidden.jvtaskmanagementsystem.dto.task;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateSubtaskRequestDto(@NotBlank @Size(max = 200) String name, @JsonProperty("completed") boolean isCompleted) {}

