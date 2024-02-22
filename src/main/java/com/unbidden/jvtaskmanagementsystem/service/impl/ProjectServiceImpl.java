package com.unbidden.jvtaskmanagementsystem.service.impl;

import com.unbidden.jvtaskmanagementsystem.dto.project.CreateProjectRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.ProjectResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.UpdateProjectRoleRequestDto;
import com.unbidden.jvtaskmanagementsystem.exception.EntityNotFoundException;
import com.unbidden.jvtaskmanagementsystem.mapper.ProjectMapper;
import com.unbidden.jvtaskmanagementsystem.model.Project;
import com.unbidden.jvtaskmanagementsystem.model.Project.ProjectStatus;
import com.unbidden.jvtaskmanagementsystem.model.ProjectRole;
import com.unbidden.jvtaskmanagementsystem.model.ProjectRole.ProjectRoleType;
import com.unbidden.jvtaskmanagementsystem.model.Role.RoleType;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.repository.ProjectRepository;
import com.unbidden.jvtaskmanagementsystem.repository.ProjectRoleRepository;
import com.unbidden.jvtaskmanagementsystem.repository.UserRepository;
import com.unbidden.jvtaskmanagementsystem.service.ProjectService;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository projectRepository;

    private final ProjectRoleRepository projectRoleRepository;

    private final ProjectMapper projectMapper;

    private final UserRepository userRepository;

    @Override
    public ProjectResponseDto findProjectById(User user, @NonNull Long id) {
        final Project project = getProject(id);

        if (project.isPrivate() && !isManager(user)) {
            checkUserAccessInProject(user.getId(), project.getId(), ProjectRoleType.CONTRIBUTOR);
        }
        
        updateProjectStatus(project, true);
        return projectMapper.toProjectDto(project);
    }

    @Override
    public List<ProjectResponseDto> findAllProjectsForUser(User user,
            @NonNull Pageable pageable) {
        List<ProjectRole> projectRoles = projectRoleRepository.findByUserId(user.getId());
        List<Project> projects = projectRoles.stream().map(ProjectRole::getProject).toList();

        for (Project project : projects) {
            updateProjectStatus(project, true);
        }
        return projects.stream()
                .map(projectMapper::toProjectDto)
                .toList();
    }

    @Override
    public List<ProjectResponseDto> searchProjectsByName(User user, String name,
            @NonNull Pageable pageable) {
        final boolean isManager = isManager(user);
        List<Project> projects = (isManager) ? projectRepository
                .findByNameContainsAllIgnoreCase(name, pageable) 
                : projectRepository.findPublicByNameContainsAllIgnoreCase(user.getId(),
                name, pageable);

        for (Project project : projects) {
            updateProjectStatus(project, true);
            if (!isManager) {
                project.setProjectRoles(projectRoleRepository.findByProjectId(project.getId()));
            }
        }
        return projects.stream()
                .map(projectMapper::toProjectDto)
                .toList();
    }

    @Override
    public ProjectResponseDto createProject(User user,
            @NonNull CreateProjectRequestDto requestDto) {
        final Project project = projectMapper.toProject(requestDto);
        
        ProjectRole creatorRole = new ProjectRole();
        creatorRole.setProject(project);
        creatorRole.setRoleType(ProjectRoleType.CREATOR);
        creatorRole.setUser(user);
        project.setProjectRoles(Set.of(creatorRole));
        project.setStatus(ProjectStatus.INITIATED);
        updateProjectStatus(project, false);
        return projectMapper.toProjectDto(projectRepository.save(project));
    }

    @Override
    public ProjectResponseDto updateProject(User user, @NonNull Long id,
            @NonNull CreateProjectRequestDto requestDto) {
        final Project project = getProject(id);

        if (!isManager(user)) {
            checkUserAccessInProject(user.getId(), project.getId(), ProjectRoleType.ADMIN);
        }
        
        project.setName(requestDto.getName());
        project.setDescription(requestDto.getDescription());
        project.setStartDate(requestDto.getStartDate());
        project.setEndDate(requestDto.getEndDate());
        project.setPrivate(requestDto.getIsPrivate());
        updateProjectStatus(project, false);
        return projectMapper.toProjectDto(projectRepository.save(project));
    }

    @Override
    public void deleteProject(User user, @NonNull Long id) {
        final Project project = getProject(id);

        if (!isManager(user)) {
            checkUserAccessInProject(user.getId(), project.getId(), ProjectRoleType.CREATOR);
        }

        projectRepository.deleteById(id);
    }

    @Override
    public ProjectResponseDto addUserToProject(User user, @NonNull Long projectId,
            @NonNull Long userId) {
        final Project project = getProject(projectId);
        final User newProjectMember = getUser(userId);
        
        if (!isManager(user)) {
            checkUserAccessInProject(user.getId(), project.getId(), ProjectRoleType.ADMIN);
        }
        
        ProjectRole projectRole = new ProjectRole();
        projectRole.setProject(project);
        projectRole.setRoleType(ProjectRoleType.CONTRIBUTOR);
        projectRole.setUser(newProjectMember);
        project.getProjectRoles().add(projectRole);
        updateProjectStatus(project, false);
        return projectMapper.toProjectDto(projectRepository.save(project));
    }

    @Override
    public ProjectResponseDto removeUserFromProject(User user, @NonNull Long projectId,
            @NonNull Long userId) {
        final ProjectRole projectRole = getProjectRole(projectId, userId);

        if (!isManager(user)) {
            checkUserAccessInProject(user.getId(), projectId, ProjectRoleType.ADMIN);
        }

        if (projectRole.getRoleType().equals(ProjectRoleType.CREATOR)) {
            throw new UnsupportedOperationException(
                    "Project creator cannot be removed from the project.");
        }

        projectRoleRepository.delete(projectRole);
        return projectMapper.toProjectDto(getProject(projectId));
    }

    @Override
    public ProjectResponseDto changeProjectMemberRole(User user, @NonNull Long projectId,
            @NonNull Long userId, @NonNull UpdateProjectRoleRequestDto requestDto) {
        final Project project = getProject(projectId);

        updateProjectStatus(project, true);

        ProjectRole creatorRole = (isManager(user)) ? project.getProjectRoles().stream()
                .filter(pr -> pr.getRoleType().equals(ProjectRoleType.CREATOR))
                .toList()
                .get(0) 
                : checkUserAccessInProject(user.getId(), project.getId(),
                ProjectRoleType.CREATOR);       

        ProjectRole targetUserProjectRole = projectRoleRepository
                .findByProjectIdWithUserId(projectId, userId).orElseThrow(() -> 
                new EntityNotFoundException("User with id " + userId + " does not exist or " 
                + "they are not a member of project with id " + projectId));

        if (requestDto.getNewRole().equals(ProjectRoleType.CREATOR)) {
            creatorRole.setRoleType(ProjectRoleType.ADMIN);
            projectRoleRepository.save(creatorRole);
        }
        targetUserProjectRole.setRoleType(requestDto.getNewRole());
        projectRoleRepository.save(targetUserProjectRole);
        return projectMapper.toProjectDto(getProject(projectId));
    }

    private ProjectRole checkUserAccessInProject(Long userId, Long projectId,
            ProjectRoleType roleRequiredForAccess) {
        ProjectRole projectRole = getProjectRole(projectId, userId);
        
        if (projectRole.getRoleType().compareTo(roleRequiredForAccess) > 0) {
            throw new AccessDeniedException("User does not have required project role <"
                    + roleRequiredForAccess + "> to access this resource.");
        }
        return projectRole;
    }
    
    private boolean isManager(@NonNull User user) {
        for (GrantedAuthority grantedAuthority : user.getAuthorities()) {
            if (grantedAuthority.getAuthority().equals("ROLE_" + RoleType.MANAGER)) {
                return true;
            }      
        }
        return false;
    }

    private void updateProjectStatus(Project project, boolean save) {
        final LocalDate currentDate = LocalDate.now();
        final ProjectStatus initialStatus = project.getStatus();

        if (project.getStartDate().isAfter(currentDate) 
                && !project.getStatus().equals(ProjectStatus.INITIATED)) {
            project.setStatus(ProjectStatus.INITIATED);
        }

        if (project.getStartDate().isBefore(currentDate) 
                && project.getEndDate().isAfter(currentDate)
                && !project.getStatus().equals(ProjectStatus.IN_PROGRESS)) {
            project.setStatus(ProjectStatus.IN_PROGRESS);
        }

        if (project.getEndDate().isBefore(currentDate)
                && !project.getStatus().equals(ProjectStatus.COMPLETED)) {
            project.setStatus(ProjectStatus.COMPLETED);
        }

        if (save && !initialStatus.equals(project.getStatus())) {
            projectRepository.save(project);
        }
    }

    private Project getProject(@NonNull Long projectId) {
        return projectRepository.findById(projectId).orElseThrow(() ->
                new EntityNotFoundException("Was not able to find a project with id "
                + projectId));
    }

    private User getUser(@NonNull Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException("Was not able to find a user with id " 
                + userId));
    }

    private ProjectRole getProjectRole(Long projectId, Long userId) {
        return projectRoleRepository.findByProjectIdWithUserId(
            projectId, userId).orElseThrow(() -> new AccessDeniedException(
            "User is not a member of this project."));
    }
}
