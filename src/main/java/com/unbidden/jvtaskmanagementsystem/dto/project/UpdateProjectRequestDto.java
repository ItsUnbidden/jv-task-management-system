package com.unbidden.jvtaskmanagementsystem.dto.project;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Data;

@Data
public class UpdateProjectRequestDto {
    @Size(min = 3, max = 50)
    private String name;

    private String description;

    private LocalDate startDate;

    private LocalDate endDate;

    private Boolean isPrivate;
}
