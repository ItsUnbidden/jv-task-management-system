package com.unbidden.jvtaskmanagementsystem.dto.label;

import java.util.Set;
import lombok.Data;

@Data
public class LabelResponseDto {
    private Long id;

    private String name;

    private String color;

    private Long projectId;

    private Set<Long> taskIds;
}
