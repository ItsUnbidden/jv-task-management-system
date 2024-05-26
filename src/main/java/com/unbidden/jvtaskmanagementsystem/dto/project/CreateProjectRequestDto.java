package com.unbidden.jvtaskmanagementsystem.dto.project;

import com.unbidden.jvtaskmanagementsystem.validation.DateConsistency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
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

    private boolean isPrivate;
}
