package com.unbidden.jvtaskmanagementsystem.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;

import com.unbidden.jvtaskmanagementsystem.dto.project.UpdateProjectRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.UpdateProjectStatusRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.projectrole.UpdateProjectRoleRequestDto;
import com.unbidden.jvtaskmanagementsystem.model.Project;
import com.unbidden.jvtaskmanagementsystem.model.User;

public interface ProjectService {
    @NonNull
    public Project findProjectById(@NonNull Long projectId);
    
    @NonNull
    public Page<Project> findAllProjectsForUserAndSearchByName(@NonNull User user,
            @NonNull String name, @NonNull Pageable pageable);

    @NonNull
    public Page<Project> searchProjectsByName(@NonNull User user, 
            @NonNull String name, @NonNull Pageable pageable);
    
    @NonNull
    public Project createProject(@NonNull User user, @NonNull Project project);
    
    @NonNull
    public Project updateProject(@NonNull Long projectId,
            @NonNull UpdateProjectRequestDto requestDto);

    public void deleteProject(@NonNull Long projectId);

    @NonNull
    public Project addUserToProject(@NonNull Long projectId,
            @NonNull String username);

    @NonNull
    public Project changeProjectMemberRole(@NonNull Long projectId, @NonNull Long userId,
            @NonNull UpdateProjectRoleRequestDto requestDto);

    @NonNull
    public void removeUserFromProject(@NonNull Long projectId, @NonNull Long userId);

    public void quitProject(@NonNull User user, @NonNull Long projectId);

    @NonNull
    public Project changeStatus(@NonNull Long projectId,
            @NonNull UpdateProjectStatusRequestDto requestDto);

    @NonNull
    public Project connectProjectToDropbox(@NonNull Project project);

    @NonNull
    public Project disconnectDropbox(@NonNull Project project);

    @NonNull
    public Project disconnectCalendar(@NonNull Project project);
}
