package com.unbidden.jvtaskmanagementsystem.service.util;

import com.unbidden.jvtaskmanagementsystem.exception.EntityNotFoundException;
import com.unbidden.jvtaskmanagementsystem.model.Project;
import com.unbidden.jvtaskmanagementsystem.model.ProjectRole;
import com.unbidden.jvtaskmanagementsystem.model.Role.RoleType;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.repository.ProjectRepository;
import com.unbidden.jvtaskmanagementsystem.repository.ProjectRoleRepository;
import com.unbidden.jvtaskmanagementsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@SuppressWarnings("null")
public class EntityUtil {
    private final ProjectRepository projectRepository;

    private final ProjectRoleRepository projectRoleRepository;

    private final UserRepository userRepository;

    @NonNull
    public Project getProjectById(@NonNull Long projectId) {
        return projectRepository.findById(projectId).orElseThrow(() ->
                new EntityNotFoundException("Was not able to find a project with id "
                + projectId));
    }

    @NonNull
    public User getUserById(@NonNull Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException("Was not able to find a user with id " 
                + userId));
    }

    @NonNull
    public ProjectRole getProjectRoleByProjectIdAndUserId(@NonNull Long projectId,
            @NonNull Long userId) {
        return projectRoleRepository.findByProjectIdWithUserId(
                projectId, userId).orElseThrow(() -> new AccessDeniedException(
                "User is not a member of this project."));
    }

    public boolean isManager(@NonNull User user) {
        for (GrantedAuthority grantedAuthority : user.getAuthorities()) {
            if (grantedAuthority.getAuthority().equals("ROLE_" + RoleType.MANAGER)) {
                return true;
            }      
        }
        return false;
    }
}
