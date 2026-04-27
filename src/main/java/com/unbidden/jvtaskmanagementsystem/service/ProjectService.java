package com.unbidden.jvtaskmanagementsystem.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;

import com.unbidden.jvtaskmanagementsystem.dto.project.UpdateProjectRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.UpdateProjectStatusRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.projectrole.UpdateProjectRoleRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.CreatedProjectFolderResult;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.ProjectConnectedToDropboxResult;
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
    public Project createProject(@NonNull User user, @NonNull Project project,
            @NonNull CreatedProjectFolderResult dropboxResult);
    
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
    public Project removeUserFromProject(@NonNull Long projectId, @NonNull Long userId);

    public Project quitProject(@NonNull User user, @NonNull Long projectId);

    @NonNull
    public Project changeStatus(@NonNull Long projectId,
            @NonNull UpdateProjectStatusRequestDto requestDto);

    @NonNull
    public Project connectProjectToDropbox(@NonNull Long projectId,
            @NonNull ProjectConnectedToDropboxResult dropboxResult);

    @NonNull
    public Project disconnectDropbox(@NonNull Long projectId);

    @NonNull
    public Project disconnectCalendar(@NonNull Long projectId);
}
