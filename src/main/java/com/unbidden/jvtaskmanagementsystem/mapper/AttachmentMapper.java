package com.unbidden.jvtaskmanagementsystem.mapper;

import com.unbidden.jvtaskmanagementsystem.config.MapperConfig;
import com.unbidden.jvtaskmanagementsystem.dto.attachment.AttachmentDto;
import com.unbidden.jvtaskmanagementsystem.model.Attachment;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface AttachmentMapper {
    AttachmentDto toDto(Attachment attachment);

    @AfterMapping
    default void setTaskId(@MappingTarget AttachmentDto dto, Attachment attachment) {
        dto.setTaskId(attachment.getTask().getId());
    }
}
