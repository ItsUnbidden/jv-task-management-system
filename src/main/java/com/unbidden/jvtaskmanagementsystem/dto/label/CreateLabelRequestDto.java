package com.unbidden.jvtaskmanagementsystem.dto.label;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;
import lombok.Data;

@Data
public class CreateLabelRequestDto {
    @NotBlank
    @Size(min = 1, max = 25)
    private String name;

    @NotBlank
    private String color;

    @NotNull
    private Long projectId;

    @NotNull
    private Set<Long> taskIds;
}
