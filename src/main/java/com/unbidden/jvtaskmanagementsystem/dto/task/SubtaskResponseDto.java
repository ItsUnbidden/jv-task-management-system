package com.unbidden.jvtaskmanagementsystem.dto.task;

import lombok.Data;

@Data
public class SubtaskResponseDto {
    private Long id;

    private String name;

    private boolean isCompleted;

    private Long version;
}
