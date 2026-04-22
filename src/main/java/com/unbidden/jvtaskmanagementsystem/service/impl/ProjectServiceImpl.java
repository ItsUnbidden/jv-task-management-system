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
import com.unbidden.jvtaskmanagementsystem.exception.EntityNotFoundException;
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
        
        updateProjectStatusAccordingToDate(project, true);
        return project;
    }

    @NonNull
    @Override
    @Transactional
    public Page<Project> findAllProjectsForUserAndSearchByName(@NonNull User user, @NonNull String name, @NonNull Pageable pageable) {
        final Page<Project> projects = projectRepository.findProjectsForUserAndSearchByName(user.getId(), name, pageable);

        projects.forEach(p -> {
            updateProjectStatusAccordingToDate(p, true);
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
            updateProjectStatusAccordingToDate(p, true);
            p.setProjectRoles(projectRoleRepository.findByProjectId(p.getId()));
        });
        return projects;
    }

    @NonNull
    @Override
    @Transactional
    public Project createProject(@NonNull User user, @NonNull Project project) {
        ProjectRole creatorRole = new ProjectRole();
        creatorRole.setProject(project);
        creatorRole.setRoleType(ProjectRoleType.CREATOR);
        creatorRole.setUser(user);
        project.setProjectRoles(Set.of(creatorRole));
        project.setStatus(ProjectStatus.INITIATED);
        project.setTasks(new ArrayList<>());
        if (project.getStartDate() == null) {
            project.setStartDate(LocalDate.now());
        }
        
        updateProjectStatusAccordingToDate(project, false);
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
            if (requestDto.getEndDate().isAfter(project.getStartDate()) && requestDto.getEndDate().isAfter(LocalDate.now())) {
                project.setEndDate(requestDto.getEndDate());
            } else {
                throw new IllegalArgumentException("End date cannot be before start date.");
            }
        } else {
            project.setEndDate(null);
        }
        project.setPrivate(requestDto.isPrivate());
        updateProjectStatusAccordingToDate(project, false);
        return projectRepository.save(project);
    }
    
    @Override
    @Transactional
    public void deleteProject(@NonNull Long projectId) {
        final Project project = entityUtil.getProjectById(projectId);
        
        projectRepository.delete(project);
    }

    @NonNull
    @Override
    @Transactional
    public Project addUserToProject(@NonNull Long projectId, @NonNull String username) {
        final Project project = entityUtil.getProjectById(projectId);
        final User newProjectMember = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User with username " + username + " does not exist."));
    
        ProjectRole projectRole = new ProjectRole();
        projectRole.setProject(project);
        projectRole.setRoleType(ProjectRoleType.CONTRIBUTOR);
        projectRole.setUser(newProjectMember);
        project.getProjectRoles().add(projectRole);
        updateProjectStatusAccordingToDate(project, false);
        return projectRepository.save(project);
    }

    @NonNull
    @Override
    @Transactional
    public void removeUserFromProject(@NonNull Long projectId, @NonNull Long userId) {
        removeUserFromProject0(projectId, userId);
    }

    @Override
    @Transactional
    public void quitProject(@NonNull User user, @NonNull Long projectId) {
        removeUserFromProject0(projectId, user.getId());
    }

    @NonNull
    @Override
    @Transactional
    public Project changeStatus(@NonNull Long projectId,
            @NonNull UpdateProjectStatusRequestDto requestDto) {
        final Project project = entityUtil.getProjectById(projectId);

        project.setStatus(requestDto.getNewStatus());
        updateProjectStatusAccordingToDate(project, false);
        return projectRepository.save(project);
    }

    @NonNull
    @Override
    @Transactional
    public Project changeProjectMemberRole(@NonNull Long projectId,
            @NonNull Long userId, @NonNull UpdateProjectRoleRequestDto requestDto) {
        final Project project = entityUtil.getProjectById(projectId);

        updateProjectStatusAccordingToDate(project, true);

        final ProjectRole creatorRole = project.getProjectRoles().stream()
                .filter(pr -> pr.getRoleType().equals(ProjectRoleType.CREATOR))
                .toList()
                .get(0);

        final ProjectRole targetUserProjectRole = 
                entityUtil.getProjectRoleByProjectIdAndUserId(projectId, userId);

        if (requestDto.getNewRole().equals(ProjectRoleType.CREATOR)) {
            creatorRole.setRoleType(ProjectRoleType.ADMIN);
            projectRoleRepository.save(creatorRole);
        }
        targetUserProjectRole.setRoleType(requestDto.getNewRole());
        projectRoleRepository.save(targetUserProjectRole);
        return entityUtil.getProjectById(projectId);
    }

    @NonNull
    @Override
    @Transactional
    public Project connectProjectToDropbox(@NonNull Project project) {
        return projectRepository.save(project);
    }

    @NonNull
    @Override
    @Transactional
    public Project disconnectDropbox(@NonNull Project project) {
        project.setDropboxProjectFolderId(null);
        project.setDropboxProjectSharedFolderId(null);

        return projectRepository.save(project);
    }

    @NonNull
    @Override
    @Transactional
    public Project disconnectCalendar(@NonNull Project project) {
        project.setProjectCalendar(null);
        return projectRepository.save(project);
    }

    private void updateProjectStatusAccordingToDate(Project project, boolean doSave) {
        final LocalDate currentDate = LocalDate.now();
        final ProjectStatus initialStatus = project.getStatus();

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

        if (doSave && !initialStatus.equals(project.getStatus())) {
            projectRepository.save(project);
        }
    }

    private void removeUserFromProject0(Long projectId, Long userId) {
        final ProjectRole projectRole = entityUtil
                .getProjectRoleByProjectIdAndUserId(projectId, userId);
        final Project project = entityUtil.getProjectById(projectId);
        final Page<Task> userTasks = taskRepository.findByAssigneeIdAndByProjectId(
                userId, projectId, Pageable.unpaged());
        final User projectOwner = projectRoleRepository.findByRoleType(ProjectRoleType.CREATOR)
                .get(0).getUser();
             
        userTasks.forEach(t -> t.setAssignee(projectOwner));
        project.getProjectRoles().removeIf(pr -> pr.getId().equals(projectRole.getId()));
        projectRoleRepository.delete(projectRole);
        taskRepository.saveAll(userTasks);
        projectRepository.save(project);
    }
}
