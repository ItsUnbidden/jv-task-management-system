package com.unbidden.jvtaskmanagementsystem.dto.label;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateLabelRequestDto {
    @NotBlank
    @Size(min = 1, max = 25)
    private String name;

    @NotNull
    private String color;

    @NotNull
    private Set<Long> taskIds;
}
