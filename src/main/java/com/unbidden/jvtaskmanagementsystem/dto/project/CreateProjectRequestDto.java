package com.unbidden.jvtaskmanagementsystem.dto.project;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.unbidden.jvtaskmanagementsystem.validation.DateConsistency;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@DateConsistency
public class CreateProjectRequestDto {
    @NotBlank
    @Size(min = 3, max = 50)
    private String name;

    private String description;

    private LocalDate startDate;
    
    private LocalDate endDate;

    @JsonProperty("isPrivate")
    private boolean isPrivate;
}
