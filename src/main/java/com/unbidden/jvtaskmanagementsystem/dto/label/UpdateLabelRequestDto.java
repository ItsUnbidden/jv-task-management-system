package com.unbidden.jvtaskmanagementsystem.dto.label;

import jakarta.validation.constraints.Size;
import java.util.Set;
import lombok.Data;

@Data
public class UpdateLabelRequestDto {
    @Size(min = 1, max = 25)
    private String name;

    private String color;

    private Set<Long> taskIds;
}
