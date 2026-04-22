package com.unbidden.jvtaskmanagementsystem.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.unbidden.jvtaskmanagementsystem.config.MapperConfig;
import com.unbidden.jvtaskmanagementsystem.dto.task.CreateTaskRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.TaskResponseDto;
import com.unbidden.jvtaskmanagementsystem.model.Label;
import com.unbidden.jvtaskmanagementsystem.model.Task;

@Mapper(config = MapperConfig.class)
public interface TaskMapper {
    @Mapping(target = "assigneeId", source = "assignee.id")
    @Mapping(target = "assigneeUsername", source = "assignee.username")
    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectName", source = "project.name")
    @Mapping(target = "labelIds", source = "labels")
    TaskResponseDto toDto(Task task);

    @Mapping(target = "status", ignore = true)
    @Mapping(target = "dropboxTaskFolderId", ignore = true)
    @Mapping(target = "amountOfMessages", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "labels", ignore = true)
    @Mapping(target = "assignee", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Task toModel(CreateTaskRequestDto requestDto);

    default Long lablesToLabelIds(Label label) {
        if (label != null) return label.getId();
        return 0L;
    }
}
