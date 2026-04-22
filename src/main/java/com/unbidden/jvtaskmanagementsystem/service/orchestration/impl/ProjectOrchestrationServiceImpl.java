package com.unbidden.jvtaskmanagementsystem.service.orchestration.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.unbidden.jvtaskmanagementsystem.dto.project.AddNewUserToProjectResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.CreateProjectRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.DeleteProjectResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.ProjectCalendarDisconnectionResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.ProjectDropboxDisconnectionResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.ProjectResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.RemoveUserFromProjectResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.UpdateProjectRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.UpdateProjectStatusRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.internal.CreatedProjectFolderResult;
import com.unbidden.jvtaskmanagementsystem.dto.project.internal.ProjectConnectedToDropboxResult;
import com.unbidden.jvtaskmanagementsystem.dto.projectrole.UpdateProjectRoleRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.ThirdPartyOperationResult;
import com.unbidden.jvtaskmanagementsystem.exception.EntityNotFoundException;
import com.unbidden.jvtaskmanagementsystem.mapper.ProjectMapper;
import com.unbidden.jvtaskmanagementsystem.model.Project;
import com.unbidden.jvtaskmanagementsystem.model.ProjectRole;
import com.unbidden.jvtaskmanagementsystem.model.ProjectRole.ProjectRoleType;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.repository.UserRepository;
import com.unbidden.jvtaskmanagementsystem.security.project.ProjectSecurity;
import com.unbidden.jvtaskmanagementsystem.service.DropboxService;
import com.unbidden.jvtaskmanagementsystem.service.GoogleCalendarService;
import com.unbidden.jvtaskmanagementsystem.service.ProjectService;
import com.unbidden.jvtaskmanagementsystem.service.orchestration.ProjectOrchestrationService;
import com.unbidden.jvtaskmanagementsystem.util.EntityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectOrchestrationServiceImpl implements ProjectOrchestrationService {
    private final ProjectService projectService;

    private final DropboxService dropboxService;

    private final GoogleCalendarService calendarService;

    private final ProjectMapper projectMapper;

    private final UserRepository userRepository;

    private final EntityUtil entityUtil;

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, bypassIfPublic = true)
    public ProjectResponseDto findProjectById(@NonNull User user, @NonNull Long projectId) {
        return projectMapper.toProjectDto(projectService.findProjectById(projectId));
    }

    @NonNull
    @Override
    public Page<ProjectResponseDto> findAllProjectsForUserAndSearchByName(@NonNull User user,
            @NonNull String name, @NonNull Pageable pageable) {
        return projectService.findAllProjectsForUserAndSearchByName(user, name, pageable).map(projectMapper::toProjectDto);
    }

    @NonNull
    @Override
    public Page<ProjectResponseDto> searchProjectsByName(@NonNull User user,
            @NonNull String name, @NonNull Pageable pageable) {
        return projectService.searchProjectsByName(user, name, pageable).map(projectMapper::toProjectDto);
    }

    @NonNull
    @Override
    public ProjectResponseDto createProject(@NonNull User user,
            @NonNull CreateProjectRequestDto requestDto) {
        final Project project = projectMapper.toProject(requestDto);
        
        final CreatedProjectFolderResult dropboxResult =
                dropboxService.createSharedProjectFolder(user, project);
        final Project response = projectService.createProject(user, project, dropboxResult);
        
        calendarService.createCalendarForProject(user, project);
        return projectMapper.toProjectDto(response);
    }

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.ADMIN)
    public ProjectResponseDto updateProject(@NonNull User user, @NonNull Long projectId,
            @NonNull UpdateProjectRequestDto requestDto) {
        final Project project = projectService.updateProject(projectId, requestDto);
        final User authorizedUser = (entityUtil.isManager(user))
                ? entityUtil.getProjectOwner(project) : user;

        calendarService.changeProjectEventsDates(authorizedUser, project,
                requestDto.getStartDate(), requestDto.getEndDate());
        return projectMapper.toProjectDto(project);
    }

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CREATOR)
    public DeleteProjectResponseDto deleteProject(@NonNull User user, @NonNull Long projectId) {
        final Project project = entityUtil.getProjectById(projectId);
        final User authorizedUser = (entityUtil.isManager(user))
                ? entityUtil.getProjectOwner(project) : user;

        final DeleteProjectResponseDto responseDto = new DeleteProjectResponseDto(
                project.getName(),
                dropboxService.deleteProjectFolder(authorizedUser, project),
                calendarService.deleteProjectCalendar(authorizedUser, project));

        projectService.deleteProject(projectId);
        return responseDto;
    }

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.ADMIN)
    public AddNewUserToProjectResponseDto addUserToProject(@NonNull User user, @NonNull Long projectId,
            @NonNull String username) {
        final Project project = entityUtil.getProjectById(projectId);
        final User newProjectMember = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User with username " + username + " does not exist."));
        final User authorizedUser = (entityUtil.isManager(user))
                ? entityUtil.getProjectOwner(project) : user;

        if (!project.getProjectRoles().stream()
                .filter(pr -> pr.getUser().getId().equals(newProjectMember.getId()))
                .toList().isEmpty()) {
            throw new UnsupportedOperationException("User " + username 
                    + " is already a member of project with id " + projectId);
        }
        final AddNewUserToProjectResponseDto responseDto = new AddNewUserToProjectResponseDto(
                projectMapper.toProjectDto(projectService.addUserToProject(projectId, username)),
                dropboxService.addProjectMemberToSharedFolder(authorizedUser, newProjectMember, project),
                calendarService.addUserToCalendar(project, newProjectMember));

        return responseDto;
    }

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CREATOR)
    public ProjectResponseDto changeProjectMemberRole(@NonNull User user, @NonNull Long projectId,
            @NonNull Long userId, @NonNull UpdateProjectRoleRequestDto requestDto) {
        final Project project = entityUtil.getProjectById(projectId);
        final User authorizedUser = (entityUtil.isManager(user))
                ? entityUtil.getProjectOwner(project) : user;

        if (requestDto.getNewRole().equals(ProjectRoleType.CREATOR)) {
            final User userToTrasferTo = entityUtil.getUserById(userId);

            dropboxService.transferOwnership(authorizedUser, userToTrasferTo, project);
            calendarService.transferOwnership(authorizedUser, project, userToTrasferTo);
        }
        return projectMapper.toProjectDto(projectService.changeProjectMemberRole(projectId, userId, requestDto));
    }

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.ADMIN)
    public RemoveUserFromProjectResponseDto removeUserFromProject(@NonNull User user, @NonNull Long projectId,
            @NonNull Long userId) {
        final ProjectRole projectRole = entityUtil
                .getProjectRoleByProjectIdAndUserId(projectId, userId);
        final Project project = entityUtil.getProjectById(projectId);
        final User userToRemove = (!user.getId().equals(userId)) 
                ? entityUtil.getUserById(userId) : user;
        final User authorizedUser = (entityUtil.isManager(user))
                ? entityUtil.getProjectOwner(project) : user;

        if (projectRole.getRoleType().equals(ProjectRoleType.CREATOR)) {
            throw new UnsupportedOperationException(
                    "Project creator cannot be removed from the project.");
        }
        final RemoveUserFromProjectResponseDto responseDto = new RemoveUserFromProjectResponseDto(
                projectMapper.toProjectDto(projectService.removeUserFromProject(projectId, userId)),
                dropboxService.removeMemberFromSharedFolder(authorizedUser, userToRemove, project),
                calendarService.removeUserFromCalendar(project, userToRemove));

        return responseDto;
    }

    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR)
    public RemoveUserFromProjectResponseDto quitProject(@NonNull User user, @NonNull Long projectId) {
        final ProjectRole projectRole = entityUtil
                .getProjectRoleByProjectIdAndUserId(projectId, user.getId());
        final Project project = entityUtil.getProjectById(projectId);
        final User authorizedUser = (entityUtil.isManager(user))
                ? entityUtil.getProjectOwner(project) : user;

        if (projectRole.getRoleType().equals(ProjectRoleType.CREATOR)) {
            throw new UnsupportedOperationException(
                    "Project creator cannot be removed from the project.");
        }
        final RemoveUserFromProjectResponseDto responseDto = new RemoveUserFromProjectResponseDto(
                projectMapper.toProjectDto(projectService.removeUserFromProject(projectId, user.getId())),
                dropboxService.removeMemberFromSharedFolder(authorizedUser, user, project),
                calendarService.removeUserFromCalendar(project, user));
        
        return responseDto;
    }

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CREATOR)
    public ProjectResponseDto changeStatus(@NonNull User user, @NonNull Long projectId,
            @NonNull UpdateProjectStatusRequestDto requestDto) {
        return projectMapper.toProjectDto(projectService.changeStatus(projectId, requestDto));
    }

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CREATOR)
    public ProjectResponseDto connectProjectToDropbox(@NonNull User user,
            @NonNull Long projectId) {
        final Project project = entityUtil.getProjectById(projectId);
        final User authorizedUser = (entityUtil.isManager(user))
                ? entityUtil.getProjectOwner(project) : user;
        
        final ProjectConnectedToDropboxResult dropboxResult =
                dropboxService.connectProjectToDropbox(authorizedUser, project);
        return projectMapper.toProjectDto(projectService.connectProjectToDropbox(project.getId(),
                dropboxResult));
    }

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CREATOR)
    public ProjectResponseDto connectProjectToCalendar(@NonNull User user,
            @NonNull Long projectId) {
        final Project project = entityUtil.getProjectById(projectId);
        final User authorizedUser = (entityUtil.isManager(user))
                ? entityUtil.getProjectOwner(project) : user;
        
        calendarService.connectProjectToCalendar(authorizedUser, project);
        return projectMapper.toProjectDto(project);
    }

    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR)
    public void joinDropbox(@NonNull User user, @NonNull Long projectId) {
        final Project project = entityUtil.getProjectById(projectId);
        final boolean isProjectMember = !project.getProjectRoles().stream()
                .filter(pr -> pr.getUser().equals(user))
                .toList().isEmpty();

        if (!isProjectMember) {
            throw new UnsupportedOperationException("Only project members "
                    + "can call this endpoint.");
        }
        dropboxService.joinDropbox(user, project);
    }

    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR)
    public void joinCalendar(@NonNull User user, @NonNull Long projectId) {
        final Project project = entityUtil.getProjectById(projectId);
        final boolean isProjectMember = !project.getProjectRoles().stream()
                .filter(pr -> pr.getUser().equals(user))
                .toList().isEmpty();

        if (!isProjectMember) {
            throw new UnsupportedOperationException("Only project members "
                    + "can call this endpoint.");
        }
        calendarService.joinCalendar(user, project);
    }

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CREATOR)
    public ProjectDropboxDisconnectionResponseDto disconnectDropbox(@NonNull User user, @NonNull Long projectId) {
        final Project project = entityUtil.getProjectById(projectId);
        final ThirdPartyOperationResult dropboxResult = dropboxService.deleteProjectFolder(user, project);
        
	return new ProjectDropboxDisconnectionResponseDto(projectMapper.toProjectDto(
                projectService.disconnectDropbox(project.getId())), dropboxResult);
    }

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CREATOR)
    public ProjectCalendarDisconnectionResponseDto disconnectCalendar(@NonNull User user, @NonNull Long projectId) {
        final Project project = entityUtil.getProjectById(projectId);
        final ThirdPartyOperationResult calendarResult = calendarService.deleteProjectCalendar(user, project);
        
        return new ProjectCalendarDisconnectionResponseDto(projectMapper.toProjectDto(
                projectService.disconnectCalendar(project.getId())), calendarResult);
    }
}
