package com.unbidden.jvtaskmanagementsystem.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.unbidden.jvtaskmanagementsystem.config.MapperConfig;
import com.unbidden.jvtaskmanagementsystem.dto.label.CreateLabelRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.label.LabelResponseDto;
import com.unbidden.jvtaskmanagementsystem.model.Label;
import com.unbidden.jvtaskmanagementsystem.model.Task;

@Mapper(config = MapperConfig.class)
public interface LabelMapper {
    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "taskIds", source = "tasks")
    LabelResponseDto toDto(Label label);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Label toModel(CreateLabelRequestDto requestDto);

    default Long mapTaskId(Task task) {
        return task.getId();
    }
}
