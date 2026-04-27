package com.unbidden.jvtaskmanagementsystem.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unbidden.jvtaskmanagementsystem.dto.project.UpdateProjectRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.UpdateProjectStatusRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.projectrole.UpdateProjectRoleRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.ThirdPartyOperationResult.ThirdPartyOperationStatus;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.AddUserToProjectFolderResult;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.CreatedProjectFolderResult;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.CreatedTaskFolderResult;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.ProjectConnectedToDropboxResult;
import com.unbidden.jvtaskmanagementsystem.exception.EntityNotFoundException;
import com.unbidden.jvtaskmanagementsystem.exception.ErrorType;
import com.unbidden.jvtaskmanagementsystem.model.Project;
import com.unbidden.jvtaskmanagementsystem.model.Project.ProjectStatus;
import com.unbidden.jvtaskmanagementsystem.model.ProjectRole;
import com.unbidden.jvtaskmanagementsystem.model.ProjectRole.ProjectRoleType;
import com.unbidden.jvtaskmanagementsystem.model.Task;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.repository.ProjectRepository;
import com.unbidden.jvtaskmanagementsystem.repository.ProjectRoleRepository;
import com.unbidden.jvtaskmanagementsystem.repository.TaskRepository;
import com.unbidden.jvtaskmanagementsystem.repository.UserRepository;
import com.unbidden.jvtaskmanagementsystem.service.ProjectService;
import com.unbidden.jvtaskmanagementsystem.util.EntityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository projectRepository;

    private final ProjectRoleRepository projectRoleRepository;

    private final UserRepository userRepository;

    private final TaskRepository taskRepository;

    private final EntityUtil entityUtil;

    @NonNull
    @Override
    @Transactional
    public Project findProjectById(@NonNull Long projectId) {
        final Project project = entityUtil.getProjectById(projectId);
        
        updateProjectStatusAccordingToDate(project);
        return project;
    }

    @NonNull
    @Override
    @Transactional
    public Page<Project> findAllProjectsForUserAndSearchByName(@NonNull User user, @NonNull String name, @NonNull Pageable pageable) {
        final Page<Project> projects = projectRepository.findProjectsForUserAndSearchByName(user.getId(), name, pageable);

        projects.forEach(p -> {
            updateProjectStatusAccordingToDate(p);
            p.setProjectRoles(projectRoleRepository.findByProjectId(p.getId()));
        });
        return projects;
    }

    @NonNull
    @Override
    @Transactional
    public Page<Project> searchProjectsByName(@NonNull User user, @NonNull String name,
            @NonNull Pageable pageable) {
        final boolean isManager = entityUtil.isManager(user);
        final Page<Project> projects = (isManager) ? projectRepository
                .findByNameContainsAllIgnoreCase(name, pageable) 
                : projectRepository.findPublicByNameContainsAllIgnoreCase(user.getId(),
                name, pageable);

        projects.forEach(p -> {
            updateProjectStatusAccordingToDate(p);
            p.setProjectRoles(projectRoleRepository.findByProjectId(p.getId()));
        });
        return projects;
    }

    @NonNull
    @Override
    @Transactional
    public Project createProject(@NonNull User user, @NonNull Project project,
            @NonNull CreatedProjectFolderResult dropboxResult) {
        final ProjectRole creatorRole = new ProjectRole();
        creatorRole.setProject(project);
        creatorRole.setRoleType(ProjectRoleType.CREATOR);
        creatorRole.setUser(user);
        project.setProjectRoles(Set.of(creatorRole));
        project.setStatus(ProjectStatus.INITIATED);
        project.setTasks(new ArrayList<>());
        if (project.getStartDate() == null) {
            project.setStartDate(LocalDate.now());
        }
        if (dropboxResult.getStatus().equals(ThirdPartyOperationStatus.SUCCESS)) {
            project.setDropboxProjectFolderId(dropboxResult.getProjectFolderId());
            project.setDropboxProjectSharedFolderId(dropboxResult.getProjectSharedFolderId());
        }
        
        updateProjectStatusAccordingToDate(project);
        return projectRepository.save(project);
    }

    @NonNull
    @Override
    @Transactional
    public Project updateProject(@NonNull Long projectId,
            @NonNull UpdateProjectRequestDto requestDto) {
        final Project project = entityUtil.getProjectById(projectId);
        
        project.setName(requestDto.getName());
        project.setDescription(requestDto.getDescription());
        if (requestDto.getStartDate() != null) {
            if (requestDto.getStartDate().equals(LocalDate.now()) || requestDto.getStartDate().isAfter(LocalDate.now())) {
                project.setStartDate(requestDto.getStartDate());
            }
        }
        if (requestDto.getEndDate() != null) {
            if (requestDto.getEndDate().isAfter(project.getStartDate())) {
                project.setEndDate(requestDto.getEndDate());
            } else {
                throw new IllegalArgumentException("The end date cannot be before the start date.");
            }
        } else {
            project.setEndDate(null);
        }
        project.setPrivate(requestDto.isPrivate());
        updateProjectStatusAccordingToDate(project);
        return project;
    }
    
    @Override
    @Transactional
    public void deleteProject(@NonNull Long projectId) {
        projectRepository.deleteById(projectId);
    }

    @NonNull
    @Override
    @Transactional
    public Project addUserToProject(@NonNull Long projectId, @NonNull String username) {
        final Project project = entityUtil.getProjectById(projectId);
        final User newProjectMember = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User with username "
                + username + " does not exist.", ErrorType.USER_NOT_FOUND));
    
        final ProjectRole projectRole = new ProjectRole();
        projectRole.setProject(project);
        projectRole.setRoleType(ProjectRoleType.CONTRIBUTOR);
        projectRole.setUser(newProjectMember);
        project.getProjectRoles().add(projectRole);
        updateProjectStatusAccordingToDate(project);
        return project;
    }

    @Override
    @Transactional
    public Project removeUserFromProject(@NonNull Long projectId, @NonNull Long userId) {
        return removeUserFromProject0(projectId, userId);
    }

    @Override
    @Transactional
    public Project quitProject(@NonNull User user, @NonNull Long projectId) {
        return removeUserFromProject0(projectId, user.getId());
    }

    @NonNull
    @Override
    @Transactional
    public Project changeStatus(@NonNull Long projectId,
            @NonNull UpdateProjectStatusRequestDto requestDto) {
        final Project project = entityUtil.getProjectById(projectId);

        project.setStatus(requestDto.getNewStatus());
        updateProjectStatusAccordingToDate(project);
        return project;
    }

    @NonNull
    @Override
    @Transactional
    public Project changeProjectMemberRole(@NonNull Long projectId,
            @NonNull Long userId, @NonNull UpdateProjectRoleRequestDto requestDto) {
        final Project project = entityUtil.getProjectById(projectId);

        updateProjectStatusAccordingToDate(project);

        final ProjectRole creatorRole = project.getProjectRoles().stream()
                .filter(pr -> pr.getRoleType().equals(ProjectRoleType.CREATOR))
                .toList()
                .get(0);

        final ProjectRole targetUserProjectRole = 
                entityUtil.getProjectRoleByProjectIdAndUserId(projectId, userId);

        if (requestDto.getNewRole().equals(ProjectRoleType.CREATOR)) {
            creatorRole.setRoleType(ProjectRoleType.ADMIN);
        }
        targetUserProjectRole.setRoleType(requestDto.getNewRole());
        return project;
    }

    @NonNull
    @Override
    @Transactional
    public Project connectProjectToDropbox(@NonNull Long projectId,
            @NonNull ProjectConnectedToDropboxResult dropboxResult) {
        final Project project = entityUtil.getProjectById(projectId);

        if (dropboxResult.getStatus().equals(ThirdPartyOperationStatus.SUCCESS)) {
            project.setDropboxProjectFolderId(dropboxResult.getProjectFolderResult().getProjectFolderId());
            project.setDropboxProjectSharedFolderId(dropboxResult.getProjectFolderResult().getProjectSharedFolderId());

            project.getTasks().forEach(t -> {
                final CreatedTaskFolderResult result = dropboxResult.getTaskFolderResults().get(t.getId());
                if (result != null && result.getStatus().equals(ThirdPartyOperationStatus.SUCCESS)) {
                    t.setDropboxTaskFolderId(result.getTaskFolderId());
                }
            });
            project.getProjectRoles().forEach(pr -> {
                final AddUserToProjectFolderResult result =
                        dropboxResult.getUserConnectionResults().get(pr.getUser().getId());
                if (result != null && result.getStatus().equals(ThirdPartyOperationStatus.SUCCESS)) {
                    pr.setDropboxConnected(true);
                }
            });
        }
        return project;
    }

    @NonNull
    @Override
    @Transactional
    public Project disconnectDropbox(@NonNull Long projectId) {
        final Project project = entityUtil.getProjectById(projectId);

        project.setDropboxProjectFolderId(null);
        project.setDropboxProjectSharedFolderId(null);

        return project;
    }

    @NonNull
    @Override
    @Transactional
    public Project disconnectCalendar(@NonNull Long projectId) {
        final Project project = entityUtil.getProjectById(projectId);

        project.setProjectCalendar(null);

        return project;
    }

    private void updateProjectStatusAccordingToDate(Project project) {
        final LocalDate currentDate = LocalDate.now();

        if (project.getStartDate().isAfter(currentDate) 
                && !project.getStatus().equals(ProjectStatus.INITIATED)
                && !project.getStatus().equals(ProjectStatus.COMPLETED)) {
            project.setStatus(ProjectStatus.INITIATED);
        }

        if ((project.getStartDate().isBefore(currentDate)
                || project.getStartDate().isEqual(currentDate)) 
                && !project.getStatus().equals(ProjectStatus.IN_PROGRESS)
                && !project.getStatus().equals(ProjectStatus.COMPLETED)) {
            project.setStatus(ProjectStatus.IN_PROGRESS);
        }

        if (project.getEndDate() != null 
                && project.getEndDate().isBefore(currentDate)
                && !project.getStatus().equals(ProjectStatus.COMPLETED)
                && !project.getStatus().equals(ProjectStatus.OVERDUE)) {
            project.setStatus(ProjectStatus.OVERDUE);
        }
    }

    private Project removeUserFromProject0(Long projectId, Long userId) {
        final ProjectRole projectRole = entityUtil
                .getProjectRoleByProjectIdAndUserId(projectId, userId);
        final Project project = entityUtil.getProjectById(projectId);
        final Page<Task> userTasks = taskRepository.findByAssigneeIdAndByProjectId(
                userId, projectId, Pageable.unpaged());
        final User projectOwner = projectRoleRepository.findByRoleType(ProjectRoleType.CREATOR)
                .get(0).getUser();
             
        userTasks.forEach(t -> t.setAssignee(projectOwner));
        project.getProjectRoles().remove(projectRole);
        projectRoleRepository.delete(projectRole);
        return project;
    }
}
