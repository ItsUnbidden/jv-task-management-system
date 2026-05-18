package com.unbidden.jvtaskmanagementsystem.dto.label;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateLabelRequestDto {
    @NotBlank
    @Size(max = 25)
    private String name;

    @NotBlank
    @Size(max = 50)
    private String color;

    @NotNull
    private Long projectId;

    @NotNull
    private Set<Long> taskIds;
}
