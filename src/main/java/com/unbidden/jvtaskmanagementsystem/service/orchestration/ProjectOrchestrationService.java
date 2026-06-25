package com.unbidden.jvtaskmanagementsystem.service.orchestration;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;

import com.unbidden.jvtaskmanagementsystem.dto.project.CreateProjectRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.DeleteProjectResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.ProjectCalendarDisconnectionResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.ProjectResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.ProjectWithDropboxResultResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.UpdateProjectRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.UpdateProjectStatusRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.projectrole.UpdateProjectRoleRequestDto;
import com.unbidden.jvtaskmanagementsystem.model.User;

public interface ProjectOrchestrationService {
    @NonNull
    ProjectResponseDto findProjectById(@NonNull User user, @NonNull Long projectId);
    
    @NonNull
    Page<ProjectResponseDto> findAllProjectsForUserAndSearchByName(@NonNull User user,
            @NonNull String name, @NonNull Pageable pageable);

    @NonNull
    Page<ProjectResponseDto> searchProjectsByName(@NonNull User user, 
            @NonNull String name, @NonNull Pageable pageable);
    
    @NonNull
    ProjectWithDropboxResultResponseDto createProject(@NonNull User user,
            @NonNull CreateProjectRequestDto requestDto);
    
    @NonNull
    ProjectResponseDto updateProject(@NonNull User user, @NonNull Long projectId,
            @NonNull UpdateProjectRequestDto requestDto);

    @NonNull
    DeleteProjectResponseDto deleteProject(@NonNull User user, @NonNull Long projectId);

    @NonNull
    ProjectWithDropboxResultResponseDto addUserToProject(@NonNull User user, @NonNull Long projectId,
            @NonNull String username);

    @NonNull
    ProjectWithDropboxResultResponseDto changeProjectMemberRole(@NonNull User user, @NonNull Long projectId,
            @NonNull Long userId, @NonNull UpdateProjectRoleRequestDto requestDto);

    @NonNull
    ProjectWithDropboxResultResponseDto removeUserFromProject(@NonNull User user,
            @NonNull Long projectId, @NonNull Long userId);

    @NonNull
    ProjectWithDropboxResultResponseDto quitProject(@NonNull User user, @NonNull Long projectId);

    @NonNull
    ProjectResponseDto changeStatus(@NonNull User user, @NonNull Long projectId,
            @NonNull UpdateProjectStatusRequestDto requestDto);

    @NonNull
    ProjectWithDropboxResultResponseDto connectProjectToDropbox(@NonNull User user,
            @NonNull Long projectId);

    @NonNull
    ProjectResponseDto connectProjectToCalendar(@NonNull User user,
            @NonNull Long projectId);

    @NonNull
    ProjectWithDropboxResultResponseDto joinDropbox(@NonNull User user, @NonNull Long projectId);

    void joinCalendar(@NonNull User user, @NonNull Long projectId);

    @NonNull
    ProjectWithDropboxResultResponseDto disconnectDropbox(@NonNull User user, @NonNull Long projectId);

    @NonNull
    ProjectCalendarDisconnectionResponseDto disconnectCalendar(@NonNull User user, @NonNull Long projectId);

    int getProjectProgress(@NonNull User user, @NonNull Long projectId);
}
