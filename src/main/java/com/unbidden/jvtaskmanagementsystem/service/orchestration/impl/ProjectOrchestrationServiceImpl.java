package com.unbidden.jvtaskmanagementsystem.service.orchestration.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.unbidden.jvtaskmanagementsystem.dto.project.CreateProjectRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.DeleteProjectResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.ProjectCalendarDisconnectionResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.ProjectResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.ProjectWithDropboxResultResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.UpdateProjectRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.UpdateProjectStatusRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.projectrole.UpdateProjectRoleRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.ThirdPartyOperationResult;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.ThirdPartyOperationResult.ThirdPartyOperationStatus;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.AddUserToProjectFolderResult;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.CreatedProjectFolderResult;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.DeleteResult;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.DropboxOperationResult;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.DropboxOperationResult.DropboxErrorTag;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.ProjectConnectedToDropboxResult;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.RemoveUserFromProjectFolderResult;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.TransferOwnershipResult;
import com.unbidden.jvtaskmanagementsystem.exception.EntityNotFoundException;
import com.unbidden.jvtaskmanagementsystem.exception.ErrorType;
import com.unbidden.jvtaskmanagementsystem.exception.thirdparty.dropbox.DropboxConnectionException;
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
    private static final Logger LOGGER = LogManager.getFormatterLogger(ProjectOrchestrationServiceImpl.class);

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
    public ProjectWithDropboxResultResponseDto createProject(@NonNull User user,
            @NonNull CreateProjectRequestDto requestDto) {
        final Project project = projectMapper.toProject(requestDto);
        
        final CreatedProjectFolderResult dropboxResult =
                dropboxService.createSharedProjectFolder(user, project);
        final Project response = projectService.createProject(user, project, dropboxResult);
        
        calendarService.createCalendarForProject(user, project);
        return projectMapper.toProjectDtoWithDropboxResult(response, dropboxResult);
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

        final DeleteResult dropboxResult = dropboxService.deleteProjectFolder(authorizedUser, project);
        calendarService.deleteProjectCalendar(authorizedUser, project);

        projectService.deleteProject(projectId);
        return new DeleteProjectResponseDto(projectId, dropboxResult);
    }

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.ADMIN)
    public ProjectWithDropboxResultResponseDto addUserToProject(@NonNull User user, @NonNull Long projectId,
            @NonNull String username) {
        final Project project = entityUtil.getProjectById(projectId);
        final User newProjectMember = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User with username " + username + " does not exist.",
                ErrorType.USER_NOT_FOUND));
        final User authorizedUser = (entityUtil.isManager(user))
                ? entityUtil.getProjectOwner(project) : user;

        if (!project.getProjectRoles().stream()
                .filter(pr -> pr.getUser().getId().equals(newProjectMember.getId()))
                .toList().isEmpty()) {
            throw new UnsupportedOperationException("User " + username 
                    + " is already a member of project with id " + projectId);
        }
        final AddUserToProjectFolderResult dropboxResult = dropboxService
                .addProjectMemberToSharedFolder(authorizedUser, newProjectMember, project);

        checkAndDisconnectDropboxIfNeeded(user, project, dropboxResult);
        calendarService.addUserToCalendar(project, newProjectMember);

        return projectMapper.toProjectDtoWithDropboxResult(projectService.addUserToProject(projectId, username, dropboxResult),
                dropboxResult);
    }

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CREATOR)
    public ProjectWithDropboxResultResponseDto changeProjectMemberRole(@NonNull User user, @NonNull Long projectId,
            @NonNull Long userId, @NonNull UpdateProjectRoleRequestDto requestDto) {
        final Project project = entityUtil.getProjectById(projectId);
        final User authorizedUser = (entityUtil.isManager(user))
                ? entityUtil.getProjectOwner(project) : user;

        TransferOwnershipResult dropboxResult = null;
        if (requestDto.getNewRole() == ProjectRoleType.CREATOR) {
            final User userToTrasferTo = entityUtil.getUserById(userId);

            dropboxResult = dropboxService.transferOwnership(authorizedUser, userToTrasferTo, project);
            checkAndDisconnectDropboxIfNeeded(user, project, dropboxResult);

            calendarService.transferOwnership(authorizedUser, project, userToTrasferTo);
        }
        return projectMapper.toProjectDtoWithDropboxResult(projectService.changeProjectMemberRole(
                projectId, userId, requestDto), dropboxResult);
    }

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.ADMIN)
    public ProjectWithDropboxResultResponseDto removeUserFromProject(@NonNull User user, @NonNull Long projectId,
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
        final RemoveUserFromProjectFolderResult dropboxResult = dropboxService
                .removeMemberFromSharedFolder(authorizedUser, userToRemove, project);

        checkAndDisconnectDropboxIfNeeded(user, project, dropboxResult);
        calendarService.removeUserFromCalendar(project, userToRemove);

        return projectMapper.toProjectDtoWithDropboxResult(projectService.removeUserFromProject(projectId, userId),
                dropboxResult);
    }

    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR)
    public ProjectWithDropboxResultResponseDto quitProject(@NonNull User user, @NonNull Long projectId) {
        final ProjectRole projectRole = entityUtil
                .getProjectRoleByProjectIdAndUserId(projectId, user.getId());
        final Project project = entityUtil.getProjectById(projectId);
        final User authorizedUser = (entityUtil.isManager(user))
                ? entityUtil.getProjectOwner(project) : user;

        if (projectRole.getRoleType().equals(ProjectRoleType.CREATOR)) {
            throw new UnsupportedOperationException(
                    "Project creator cannot be removed from the project.");
        }
        final RemoveUserFromProjectFolderResult dropboxResult = dropboxService
                .removeMemberFromSharedFolder(authorizedUser, user, project);

        checkAndDisconnectDropboxIfNeeded(user, project, dropboxResult);
        calendarService.removeUserFromCalendar(project, user);
        return projectMapper.toProjectDtoWithDropboxResult(projectService
                .removeUserFromProject(projectId, user.getId()), dropboxResult);
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
    public ProjectWithDropboxResultResponseDto connectProjectToDropbox(@NonNull User user,
            @NonNull Long projectId) {
        final Project project = entityUtil.getProjectById(projectId);
        final User authorizedUser = (entityUtil.isManager(user))
                ? entityUtil.getProjectOwner(project) : user;
        
        final ProjectConnectedToDropboxResult dropboxResult =
                dropboxService.connectProjectToDropbox(authorizedUser, project);
        
        if (dropboxResult.getStatus() != ThirdPartyOperationStatus.SUCCESS) {
            throw new DropboxConnectionException("Failed to connect project "
                    + project.getName() + " to Dropbox due to an external error.",
                    ErrorType.PROJECT_DROPBOX_CONNECTION_ERROR, dropboxResult);
        }
        return projectMapper.toProjectDtoWithDropboxResult(projectService.connectProjectToDropbox(project.getId(),
                dropboxResult), dropboxResult);
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
    public ProjectWithDropboxResultResponseDto joinDropbox(@NonNull User user, @NonNull Long projectId) {
        final Project project = entityUtil.getProjectById(projectId);
        final boolean isProjectMember = !project.getProjectRoles().stream()
                .filter(pr -> pr.getUser().equals(user))
                .toList().isEmpty();

        if (!isProjectMember) {
            throw new UnsupportedOperationException("Only project members "
                    + "can call this endpoint.");
        }
        final AddUserToProjectFolderResult dropboxResult = dropboxService.joinDropbox(user, project);

        if (!dropboxResult.getStatus().equals(ThirdPartyOperationStatus.SUCCESS)
                && !(dropboxResult.getStatus().equals(ThirdPartyOperationStatus.PARTIAL_SUCCESS)
                && dropboxResult.getTag().equals(DropboxErrorTag.MEMBERSHIP_MOUNT_ALREADY_MOUNTED))) {
            throw new DropboxConnectionException("Failed to connect user "
                    + user.getUsername() + " to Dropbox folder for project "
                    + project.getName() + " due to an external error.",
                    ErrorType.PROJECT_JOIN_DROPBOX_ERROR, dropboxResult);
        }
        return projectMapper.toProjectDtoWithDropboxResult(
                projectService.joinProject(user, projectId), dropboxResult);
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
    public ProjectWithDropboxResultResponseDto disconnectDropbox(@NonNull User user, @NonNull Long projectId) {
        final Project project = entityUtil.getProjectById(projectId);
        final DeleteResult dropboxResult = dropboxService.deleteProjectFolder(user, project);
        
	return projectMapper.toProjectDtoWithDropboxResult(projectService.disconnectDropbox(project.getId()), dropboxResult);
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

    private void checkAndDisconnectDropboxIfNeeded(User user, Project project, DropboxOperationResult dropboxResult) {
        if (dropboxResult.getStatus() != ThirdPartyOperationStatus.SUCCESS
                && dropboxResult.getTag() == DropboxErrorTag.SHARED_FOLDER_ACCESS_INVALID_ID) {
            LOGGER.info("The shared project folder for project " + project.getName()
                    + " might not exist. The project will be disconnected from Dropbox.");
            disconnectDropbox(user, project.getId());
        }
    }
}
