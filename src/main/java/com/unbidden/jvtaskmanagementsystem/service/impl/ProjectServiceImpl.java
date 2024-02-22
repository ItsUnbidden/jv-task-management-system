package com.unbidden.jvtaskmanagementsystem.service.impl;

import com.unbidden.jvtaskmanagementsystem.dto.project.CreateProjectRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.ProjectResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.UpdateProjectRoleRequestDto;
import com.unbidden.jvtaskmanagementsystem.mapper.ProjectMapper;
import com.unbidden.jvtaskmanagementsystem.model.Project;
import com.unbidden.jvtaskmanagementsystem.model.Project.ProjectStatus;
import com.unbidden.jvtaskmanagementsystem.model.ProjectRole;
import com.unbidden.jvtaskmanagementsystem.model.ProjectRole.ProjectRoleType;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.repository.ProjectRepository;
import com.unbidden.jvtaskmanagementsystem.repository.ProjectRoleRepository;
import com.unbidden.jvtaskmanagementsystem.security.ProjectSecurity;
import com.unbidden.jvtaskmanagementsystem.service.ProjectService;
import com.unbidden.jvtaskmanagementsystem.service.util.EntityUtil;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository projectRepository;

    private final ProjectRoleRepository projectRoleRepository;

    private final ProjectMapper projectMapper;

    private final EntityUtil entityUtil;

    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, projectIdParamName = "id")
    public ProjectResponseDto findProjectById(User user, @NonNull Long id) {
        final Project project = entityUtil.getProjectById(id);
        
        updateProjectStatus(project, true);
        return projectMapper.toProjectDto(project);
    }

    @Override
    public List<ProjectResponseDto> findAllProjectsForUser(User user) {
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
        final boolean isManager = entityUtil.isManager(user);
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
    @ProjectSecurity(securityLevel = ProjectRoleType.ADMIN, projectIdParamName = "id")
    public ProjectResponseDto updateProject(User user, @NonNull Long id,
            @NonNull CreateProjectRequestDto requestDto) {
        final Project project = entityUtil.getProjectById(id);
        
        project.setName(requestDto.getName());
        project.setDescription(requestDto.getDescription());
        project.setStartDate(requestDto.getStartDate());
        project.setEndDate(requestDto.getEndDate());
        project.setPrivate(requestDto.getIsPrivate());
        updateProjectStatus(project, false);
        return projectMapper.toProjectDto(projectRepository.save(project));
    }

    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CREATOR, projectIdParamName = "id")
    public void deleteProject(User user, @NonNull Long id) {
        projectRepository.deleteById(id);
    }

    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.ADMIN)
    public ProjectResponseDto addUserToProject(User user, @NonNull Long projectId,
            @NonNull Long userId) {
        final Project project = entityUtil.getProjectById(projectId);
        final User newProjectMember = entityUtil.getUserById(userId);

        if (!project.getProjectRoles().stream()
                .filter(pr -> pr.getUser().getId() == userId)
                .toList().isEmpty()) {
            throw new UnsupportedOperationException("User with id " + userId 
                    + " is already a member of project with id " + projectId);
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
    @ProjectSecurity(securityLevel = ProjectRoleType.ADMIN)
    public ProjectResponseDto removeUserFromProject(User user, @NonNull Long projectId,
            @NonNull Long userId) {
        final ProjectRole projectRole = entityUtil
                .getProjectRoleByProjectIdAndUserId(projectId, userId);

        if (projectRole.getRoleType().equals(ProjectRoleType.CREATOR)) {
            throw new UnsupportedOperationException(
                    "Project creator cannot be removed from the project.");
        }

        projectRoleRepository.delete(projectRole);
        return projectMapper.toProjectDto(entityUtil.getProjectById(projectId));
    }

    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CREATOR)
    public ProjectResponseDto changeProjectMemberRole(User user, @NonNull Long projectId,
            @NonNull Long userId, @NonNull UpdateProjectRoleRequestDto requestDto) {
        final Project project = entityUtil.getProjectById(projectId);

        updateProjectStatus(project, true);

        ProjectRole creatorRole = project.getProjectRoles().stream()
                .filter(pr -> pr.getRoleType().equals(ProjectRoleType.CREATOR))
                .toList()
                .get(0);

        ProjectRole targetUserProjectRole = 
                entityUtil.getProjectRoleByProjectIdAndUserId(projectId, userId);

        if (requestDto.getNewRole().equals(ProjectRoleType.CREATOR)) {
            creatorRole.setRoleType(ProjectRoleType.ADMIN);
            projectRoleRepository.save(creatorRole);
        }
        targetUserProjectRole.setRoleType(requestDto.getNewRole());
        projectRoleRepository.save(targetUserProjectRole);
        return projectMapper.toProjectDto(entityUtil.getProjectById(projectId));
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
}
