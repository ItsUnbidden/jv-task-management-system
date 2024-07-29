package com.unbidden.jvtaskmanagementsystem.service.impl;

import com.unbidden.jvtaskmanagementsystem.dto.project.CreateProjectRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.ProjectResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.UpdateProjectRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.UpdateProjectStatusRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.projectrole.UpdateProjectRoleRequestDto;
import com.unbidden.jvtaskmanagementsystem.mapper.ProjectMapper;
import com.unbidden.jvtaskmanagementsystem.model.Project;
import com.unbidden.jvtaskmanagementsystem.model.Project.ProjectStatus;
import com.unbidden.jvtaskmanagementsystem.model.ProjectRole;
import com.unbidden.jvtaskmanagementsystem.model.ProjectRole.ProjectRoleType;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.repository.ProjectRepository;
import com.unbidden.jvtaskmanagementsystem.repository.ProjectRoleRepository;
import com.unbidden.jvtaskmanagementsystem.security.project.ProjectSecurity;
import com.unbidden.jvtaskmanagementsystem.service.DropboxService;
import com.unbidden.jvtaskmanagementsystem.service.GoogleCalendarService;
import com.unbidden.jvtaskmanagementsystem.service.ProjectService;
import com.unbidden.jvtaskmanagementsystem.util.EntityUtil;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository projectRepository;

    private final ProjectRoleRepository projectRoleRepository;

    private final ProjectMapper projectMapper;

    private final EntityUtil entityUtil;

    private final DropboxService dropboxService;

    private final GoogleCalendarService calendarService;

    @Value("${dropbox.root.path}")
    private String dropboxRootPath;

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, bypassIfPublic = true)
    public ProjectResponseDto findProjectById(@NonNull User user, @NonNull Long projectId) {
        final Project project = entityUtil.getProjectById(projectId);
        
        updateProjectStatusAccordingToDate(project, true);
        return projectMapper.toProjectDto(project);
    }

    @NonNull
    @Override
    public List<ProjectResponseDto> findAllProjectsForUser(@NonNull User user) {
        List<ProjectRole> projectRoles = projectRoleRepository.findByUserId(user.getId());
        List<Project> projects = projectRoles.stream().map(ProjectRole::getProject).toList();

        projects.stream().forEach(p -> updateProjectStatusAccordingToDate(p, true));
        return projects.stream()
                .map(projectMapper::toProjectDto)
                .toList();
    }

    @NonNull
    @Override
    public List<ProjectResponseDto> searchProjectsByName(@NonNull User user, String name,
            @NonNull Pageable pageable) {
        final boolean isManager = entityUtil.isManager(user);
        List<Project> projects = (isManager) ? projectRepository
                .findByNameContainsAllIgnoreCase(name, pageable) 
                : projectRepository.findPublicByNameContainsAllIgnoreCase(user.getId(),
                name, pageable);

        for (Project project : projects) {
            updateProjectStatusAccordingToDate(project, true);
            if (!isManager) {
                project.setProjectRoles(projectRoleRepository.findByProjectId(project.getId()));
            }
        }
        return projects.stream()
                .map(projectMapper::toProjectDto)
                .toList();
    }

    @NonNull
    @Override
    public ProjectResponseDto createProject(@NonNull User user,
            @NonNull CreateProjectRequestDto requestDto) {
        final Project project = projectMapper.toProject(requestDto);
        
        dropboxService.createSharedProjectFolder(user, project);
        ProjectRole creatorRole = new ProjectRole();
        creatorRole.setProject(project);
        creatorRole.setRoleType(ProjectRoleType.CREATOR);
        creatorRole.setUser(user);
        project.setProjectRoles(Set.of(creatorRole));
        project.setStatus(ProjectStatus.INITIATED);
        project.setTasks(new ArrayList<>());
        if (requestDto.getStartDate() == null) {
            project.setStartDate(LocalDate.now());
        }
        
        updateProjectStatusAccordingToDate(project, false);
        projectRepository.save(project);
        calendarService.createCalendarForProject(user, project);
        return projectMapper.toProjectDto(project);
    }

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.ADMIN)
    public ProjectResponseDto updateProject(@NonNull User user, @NonNull Long projectId,
            @NonNull UpdateProjectRequestDto requestDto) {
        final Project project = entityUtil.getProjectById(projectId);
        
        calendarService.changeProjectEventsDates(user, project,
                requestDto.getStartDate(), requestDto.getEndDate());
        project.setName(requestDto.getName());
        project.setDescription(requestDto.getDescription());
        project.setStartDate(requestDto.getStartDate());
        project.setEndDate(requestDto.getEndDate());
        project.setPrivate(requestDto.isPrivate());
        updateProjectStatusAccordingToDate(project, false);
        return projectMapper.toProjectDto(projectRepository.save(project));
    }
    //TODO: Prevent managers from interfering in third-party actions
    
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CREATOR)
    public void deleteProject(@NonNull User user, @NonNull Long projectId) {
        final Project project = entityUtil.getProjectById(projectId);
        
        dropboxService.deleteProjectFolder(user, project);
        calendarService.deleteProjectCalendar(user, project);
        projectRepository.delete(project);
    }

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.ADMIN)
    public ProjectResponseDto addUserToProject(@NonNull User user,
            @NonNull Long projectId, @NonNull Long userId) {
        final Project project = entityUtil.getProjectById(projectId);
        final User newProjectMember = entityUtil.getUserById(userId);

        if (!project.getProjectRoles().stream()
                .filter(pr -> pr.getUser().getId() == userId)
                .toList().isEmpty()) {
            throw new UnsupportedOperationException("User with id " + userId 
                    + " is already a member of project with id " + projectId);
        }

        dropboxService.addProjectMemberToSharedFolder(user, newProjectMember, project);
        calendarService.addUserToCalendar(project, newProjectMember);
        ProjectRole projectRole = new ProjectRole();
        projectRole.setProject(project);
        projectRole.setRoleType(ProjectRoleType.CONTRIBUTOR);
        projectRole.setUser(newProjectMember);
        project.getProjectRoles().add(projectRole);
        updateProjectStatusAccordingToDate(project, false);
        return projectMapper.toProjectDto(projectRepository.save(project));
    }

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.ADMIN)
    public ProjectResponseDto removeUserFromProject(@NonNull User user,
            @NonNull Long projectId, @NonNull Long userId) {
        return removeUserFromProject0(user, projectId, userId);
    }

    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR)
    public void quitProject(@NonNull User user, @NonNull Long projectId) {
        removeUserFromProject0(user, projectId, user.getId());
    }

    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CREATOR)
    public ProjectResponseDto changeStatus(@NonNull User user, @NonNull Long projectId,
            @NonNull UpdateProjectStatusRequestDto requestDto) {
        final Project project = entityUtil.getProjectById(projectId);

        project.setStatus(requestDto.getNewStatus());
        updateProjectStatusAccordingToDate(project, false);
        return projectMapper.toProjectDto(projectRepository.save(project));
    }

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CREATOR)
    public ProjectResponseDto changeProjectMemberRole(@NonNull User user, @NonNull Long projectId,
            @NonNull Long userId, @NonNull UpdateProjectRoleRequestDto requestDto) {
        final Project project = entityUtil.getProjectById(projectId);

        updateProjectStatusAccordingToDate(project, true);

        ProjectRole creatorRole = project.getProjectRoles().stream()
                .filter(pr -> pr.getRoleType().equals(ProjectRoleType.CREATOR))
                .toList()
                .get(0);

        ProjectRole targetUserProjectRole = 
                entityUtil.getProjectRoleByProjectIdAndUserId(projectId, userId);

        if (requestDto.getNewRole().equals(ProjectRoleType.CREATOR)) {
            final User userToTrasferTo = entityUtil.getUserById(userId);

            dropboxService.transferOwnership(user, userToTrasferTo, project);
            calendarService.transferOwnership(user, project, userToTrasferTo);
            creatorRole.setRoleType(ProjectRoleType.ADMIN);
            projectRoleRepository.save(creatorRole);
        }
        targetUserProjectRole.setRoleType(requestDto.getNewRole());
        projectRoleRepository.save(targetUserProjectRole);
        return projectMapper.toProjectDto(entityUtil.getProjectById(projectId));
    }

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CREATOR)
    public ProjectResponseDto connectProjectToDropbox(@NonNull User user,
            @NonNull Long projectId) {
        final Project project = entityUtil.getProjectById(projectId);
        
        dropboxService.connectProjectToDropbox(user, project);
        return projectMapper.toProjectDto(projectRepository.save(project));
    }

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CREATOR)
    public ProjectResponseDto connectProjectToCalendar(User user, Long projectId) {
        final Project project = entityUtil.getProjectById(projectId);
        
        calendarService.connectProjectToCalendar(user, project);
        return projectMapper.toProjectDto(entityUtil.getProjectById(projectId));
    }

    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR)
    public void joinCalendar(User user, Long projectId) {
        final Project project = entityUtil.getProjectById(projectId);

        calendarService.joinCalendar(user, project);
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

    private ProjectResponseDto removeUserFromProject0(User user, Long projectId, Long userId) {
        final ProjectRole projectRole = entityUtil
                .getProjectRoleByProjectIdAndUserId(projectId, userId);
        final Project project = entityUtil.getProjectById(projectId);
        final User userToRemove = (user.getId() != userId) 
                ? entityUtil.getUserById(userId) : user;

        if (projectRole.getRoleType().equals(ProjectRoleType.CREATOR)) {
            throw new UnsupportedOperationException(
                    "Project creator cannot be removed from the project.");
        }

        dropboxService.removeMemberFromSharedFolder(user, userToRemove, project);
        calendarService.removeUserFromCalendar(project, userToRemove);
        project.getProjectRoles().removeIf(pr -> pr.getId() == projectRole.getId());
        projectRoleRepository.delete(projectRole);
        return projectMapper.toProjectDto(projectRepository.save(project));
    }
}
