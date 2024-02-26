package com.unbidden.jvtaskmanagementsystem.mapper;

import com.unbidden.jvtaskmanagementsystem.config.MapperConfig;
import com.unbidden.jvtaskmanagementsystem.dto.task.CreateTaskRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.TaskResponseDto;
import com.unbidden.jvtaskmanagementsystem.model.Task;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface TaskMapper {
    TaskResponseDto toDto(Task task);

    Task toModel(CreateTaskRequestDto requestDto);

    @AfterMapping
    default void setUserAndProjectFields(@MappingTarget TaskResponseDto dto, Task task) {
        dto.setAssigneeId(task.getAssignee().getId());
        dto.setAssigneeUsername(task.getAssignee().getUsername());
        dto.setProjectId(task.getProject().getId());
        dto.setProjectName(task.getProject().getName());
    }
}
