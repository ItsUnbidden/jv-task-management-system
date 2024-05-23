package com.unbidden.jvtaskmanagementsystem.mapper;

import com.unbidden.jvtaskmanagementsystem.config.MapperConfig;
import com.unbidden.jvtaskmanagementsystem.dto.project.CreateProjectRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.ProjectResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.projectrole.ProjectRoleDto;
import com.unbidden.jvtaskmanagementsystem.model.Project;
import com.unbidden.jvtaskmanagementsystem.model.ProjectRole;
import java.util.HashSet;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface ProjectMapper {
    ProjectResponseDto toProjectDto(Project project);

    Project toProject(CreateProjectRequestDto requestDto);

    ProjectRoleDto toProjectRoleDto(ProjectRole role);

    @AfterMapping
    default void mapProjectRolesAndSetDropboxConnected(@MappingTarget ProjectResponseDto dto,
            Project project) {
        dto.setProjectRoles(new HashSet<>(project.getProjectRoles().stream()
                .map(this::toProjectRoleDto)
                .toList()));
        dto.setDropboxConnected(project.getDropboxProjectFolderId() != null);
        dto.setCalendarConnected(project.getProjectCalendar() != null);
    }

    @AfterMapping
    default void mapUsername(@MappingTarget ProjectRoleDto dto, ProjectRole role) {
        dto.setUsername(role.getUser().getUsername());
    }
}
