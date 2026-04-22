package com.unbidden.jvtaskmanagementsystem.dto.project;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProjectRequestDto {
    @NotBlank
    @Size(min = 3, max = 50)
    private String name;

    private String description;

    @NotNull
    private LocalDate startDate;
    
    private LocalDate endDate;

    @JsonProperty("isPrivate")
    private boolean isPrivate;
}
