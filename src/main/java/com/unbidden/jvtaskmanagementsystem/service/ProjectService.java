package com.unbidden.jvtaskmanagementsystem.service;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RequestParam;

import com.unbidden.jvtaskmanagementsystem.dto.project.CreateProjectRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.ProjectResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.UpdateProjectRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.UpdateProjectStatusRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.projectrole.UpdateProjectRoleRequestDto;
import com.unbidden.jvtaskmanagementsystem.model.User;

public interface ProjectService {
    @NonNull
    public ProjectResponseDto findProjectById(@NonNull User user, @NonNull Long projectId);
    
    @NonNull
    public List<ProjectResponseDto> findAllProjectsForUser(@NonNull User user, @NonNull Pageable pageable);

    @NonNull
    public List<ProjectResponseDto> searchProjectsByName(@NonNull User user, 
            @RequestParam String name, @NonNull Pageable pageable);
    
    @NonNull
    public ProjectResponseDto createProject(@NonNull User user,
            @NonNull CreateProjectRequestDto requestDto);
    
    @NonNull
    public ProjectResponseDto updateProject(@NonNull User user, @NonNull Long projectId,
            @NonNull UpdateProjectRequestDto requestDto);

    public void deleteProject(@NonNull User user, @NonNull Long projectId);

    @NonNull
    public ProjectResponseDto addUserToProject(@NonNull User user, @NonNull Long projectId,
            @NonNull String username);

    @NonNull
    public ProjectResponseDto changeProjectMemberRole(@NonNull User user, @NonNull Long projectId,
            @NonNull Long userId, @NonNull UpdateProjectRoleRequestDto requestDto);

    @NonNull
    public ProjectResponseDto removeUserFromProject(@NonNull User user,
            @NonNull Long projectId, @NonNull Long userId);

    public void quitProject(@NonNull User user, @NonNull Long projectId);

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
    public ProjectResponseDto disconnectDropbox(@NonNull User user, @NonNull Long projectId);

    @NonNull
    public ProjectResponseDto disconnectCalendar(@NonNull User user, @NonNull Long projectId);
}
