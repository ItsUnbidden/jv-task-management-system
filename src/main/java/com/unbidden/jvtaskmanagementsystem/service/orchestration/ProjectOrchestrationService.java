package com.unbidden.jvtaskmanagementsystem.service.orchestration;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;

import com.unbidden.jvtaskmanagementsystem.dto.project.AddNewUserToProjectResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.CreateProjectRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.DeleteProjectResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.ProjectCalendarDisconnectionResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.ProjectDropboxDisconnectionResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.ProjectResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.RemoveUserFromProjectResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.UpdateProjectRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.UpdateProjectStatusRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.projectrole.UpdateProjectRoleRequestDto;
import com.unbidden.jvtaskmanagementsystem.model.User;

public interface ProjectOrchestrationService {
    @NonNull
    public ProjectResponseDto findProjectById(@NonNull User user, @NonNull Long projectId);
    
    @NonNull
    public Page<ProjectResponseDto> findAllProjectsForUserAndSearchByName(@NonNull User user,
            @NonNull String name, @NonNull Pageable pageable);

    @NonNull
    public Page<ProjectResponseDto> searchProjectsByName(@NonNull User user, 
            @NonNull String name, @NonNull Pageable pageable);
    
    @NonNull
    public ProjectResponseDto createProject(@NonNull User user,
            @NonNull CreateProjectRequestDto requestDto);
    
    @NonNull
    public ProjectResponseDto updateProject(@NonNull User user, @NonNull Long projectId,
            @NonNull UpdateProjectRequestDto requestDto);

    @NonNull
    public DeleteProjectResponseDto deleteProject(@NonNull User user, @NonNull Long projectId);

    @NonNull
    public AddNewUserToProjectResponseDto addUserToProject(@NonNull User user, @NonNull Long projectId,
            @NonNull String username);

    @NonNull
    public ProjectResponseDto changeProjectMemberRole(@NonNull User user, @NonNull Long projectId,
            @NonNull Long userId, @NonNull UpdateProjectRoleRequestDto requestDto);

    @NonNull
    public RemoveUserFromProjectResponseDto removeUserFromProject(@NonNull User user,
            @NonNull Long projectId, @NonNull Long userId);

    public RemoveUserFromProjectResponseDto quitProject(@NonNull User user, @NonNull Long projectId);

    @NonNull
    public ProjectResponseDto changeStatus(@NonNull User user, @NonNull Long projectId,
            @NonNull UpdateProjectStatusRequestDto requestDto);

    @NonNull
    public ProjectResponseDto connectProjectToDropbox(@NonNull User user,
            @NonNull Long projectId);

    @NonNull
    public ProjectResponseDto connectProjectToCalendar(@NonNull User user,
            @NonNull Long projectId);

    public void joinDropbox(@NonNull User user, @NonNull Long projectId);

    public void joinCalendar(@NonNull User user, @NonNull Long projectId);

    @NonNull
    public ProjectDropboxDisconnectionResponseDto disconnectDropbox(@NonNull User user, @NonNull Long projectId);

    @NonNull
    public ProjectCalendarDisconnectionResponseDto disconnectCalendar(@NonNull User user, @NonNull Long projectId);
}
