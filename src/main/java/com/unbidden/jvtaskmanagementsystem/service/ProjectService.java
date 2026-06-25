package com.unbidden.jvtaskmanagementsystem.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;

import com.unbidden.jvtaskmanagementsystem.dto.internal.TaskProgressUpdated;
import com.unbidden.jvtaskmanagementsystem.dto.project.UpdateProjectRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.UpdateProjectStatusRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.projectrole.UpdateProjectRoleRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.AddUserToProjectFolderResult;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.CreatedProjectFolderResult;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.ProjectConnectedToDropboxResult;
import com.unbidden.jvtaskmanagementsystem.model.Project;
import com.unbidden.jvtaskmanagementsystem.model.User;

public interface ProjectService {
    @NonNull
    Project findProjectById(@NonNull Long projectId);
    
    @NonNull
    Page<Project> findAllProjectsForUserAndSearchByName(@NonNull User user,
            @NonNull String name, @NonNull Pageable pageable);

    @NonNull
    Page<Project> searchProjectsByName(@NonNull User user, 
            @NonNull String name, @NonNull Pageable pageable);
    
    @NonNull
    Project createProject(@NonNull User user, @NonNull Project project,
            @NonNull CreatedProjectFolderResult dropboxResult);
    
    @NonNull
    Project updateProject(@NonNull Long projectId,
            @NonNull UpdateProjectRequestDto requestDto);

    void deleteProject(@NonNull Long projectId);

    @NonNull
    Project addUserToProject(@NonNull Long projectId,
            @NonNull String username, @NonNull AddUserToProjectFolderResult dropboxResult);

    @NonNull
    Project changeProjectMemberRole(@NonNull Long projectId, @NonNull Long userId,
            @NonNull UpdateProjectRoleRequestDto requestDto);

    @NonNull
    Project removeUserFromProject(@NonNull Long projectId, @NonNull Long userId);

    @NonNull
    Project joinProject(@NonNull User user, @NonNull Long projectId);

    @NonNull
    Project quitProject(@NonNull User user, @NonNull Long projectId);

    @NonNull
    Project changeStatus(@NonNull Long projectId,
            @NonNull UpdateProjectStatusRequestDto requestDto);

    @NonNull
    Project connectProjectToDropbox(@NonNull Long projectId,
            @NonNull ProjectConnectedToDropboxResult dropboxResult);

    @NonNull
    Project disconnectDropbox(@NonNull Long projectId);

    @NonNull
    Project disconnectCalendar(@NonNull Long projectId);

    int getProjectProgress(@NonNull Long projectId);

    void taskProgressUpdateListener(TaskProgressUpdated event);
}
