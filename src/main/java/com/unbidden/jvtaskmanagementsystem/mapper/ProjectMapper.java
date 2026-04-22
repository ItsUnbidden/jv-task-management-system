package com.unbidden.jvtaskmanagementsystem.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.unbidden.jvtaskmanagementsystem.config.MapperConfig;
import com.unbidden.jvtaskmanagementsystem.dto.project.CreateProjectRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.ProjectResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.projectrole.ProjectRoleDto;
import com.unbidden.jvtaskmanagementsystem.model.Project;
import com.unbidden.jvtaskmanagementsystem.model.ProjectRole;

@Mapper(config = MapperConfig.class)
public interface ProjectMapper {
    ProjectResponseDto toProjectDto(Project project);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "projectRoles", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "dropboxProjectFolderId", ignore = true)
    @Mapping(target = "dropboxProjectSharedFolderId", ignore = true)
    @Mapping(target = "projectCalendar", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Project toProject(CreateProjectRequestDto requestDto);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    ProjectRoleDto toProjectRoleDto(ProjectRole role);
}
