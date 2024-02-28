package com.unbidden.jvtaskmanagementsystem.mapper;

import com.unbidden.jvtaskmanagementsystem.config.MapperConfig;
import com.unbidden.jvtaskmanagementsystem.dto.label.CreateLabelRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.label.LabelResponseDto;
import com.unbidden.jvtaskmanagementsystem.model.Label;
import com.unbidden.jvtaskmanagementsystem.model.Task;
import java.util.HashSet;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface LabelMapper {
    LabelResponseDto toDto(Label label);

    Label toModel(CreateLabelRequestDto requestDto);

    @AfterMapping
    default void setTaskIdsAndProjectId(@MappingTarget LabelResponseDto dto, Label label) {
        dto.setProjectId(label.getProject().getId());
        dto.setTaskIds(new HashSet<>(label.getTasks().stream().map(Task::getId).toList()));
    }
}
