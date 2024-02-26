package com.unbidden.jvtaskmanagementsystem.security.project;

import com.unbidden.jvtaskmanagementsystem.exception.ProjectSecurityDataParsingException;
import com.unbidden.jvtaskmanagementsystem.model.Project;
import com.unbidden.jvtaskmanagementsystem.model.ProjectRole;
import com.unbidden.jvtaskmanagementsystem.model.ProjectRole.ProjectRoleType;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.service.util.EntityUtil;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ProjectSecurityAspect {
    private final EntityUtil entityUtil;

    private final ProjectProviderManager projectProviderManager;

    @Before("execution(public * com.unbidden.jvtaskmanagementsystem.service.impl..*(..)) && "
            + "@annotation(com.unbidden.jvtaskmanagementsystem.security.project.ProjectSecurity)")
    public void projectAccessAdvice(JoinPoint joinPoint) {
        ProjectSecurityDto dataFromJoinPoint = parseDataFromJoinPoint(joinPoint);

        if (dataFromJoinPoint.includePrivacyCheck 
                && !dataFromJoinPoint.project.isPrivate()) {
            return;
        }
        if (entityUtil.isManager(dataFromJoinPoint.user)) {
            return;
        }

        checkUserAccess(dataFromJoinPoint);
    }

    private void checkUserAccess(ProjectSecurityDto dto) {
        final ProjectRole projectRole = entityUtil
                .getProjectRoleByProjectIdAndUserId(dto.project.getId(),
                dto.user.getId());

        if (projectRole.getRoleType().compareTo(dto.roleRequired) > 0) {
            throw new AccessDeniedException("User does not have required project role <"
                    + dto.roleRequired + "> to access this resource.");
        }
    }

    private ProjectSecurityDto parseDataFromJoinPoint(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        ProjectSecurity annotation = signature.getMethod().getAnnotation(ProjectSecurity.class);
        String[] parameterNames = signature.getParameterNames();
        int entityIdIndex = -1;
        int userIndex = -1;

        for (int i = 0; i < parameterNames.length; i++) {
            if (parameterNames[i].equals(annotation.entityIdParamName())) {
                entityIdIndex = i;
            }
            if (parameterNames[i].equals(annotation.userParamName())) {
                userIndex = i;
            }
        }

        if (entityIdIndex == -1) {
            throw new ProjectSecurityDataParsingException("Method " + signature.getName() 
                    + "does not have a parameter with name " 
                    + annotation.entityIdParamName() + '.');
        }
        if (userIndex == -1) {
            throw new ProjectSecurityDataParsingException("Method " + signature.getName() 
                    + "does not have a parameter with name " 
                    + annotation.userParamName() + '.');
        }

        Project project = null;
        if (joinPoint.getArgs()[entityIdIndex] instanceof Long) {
            project = projectProviderManager.getProvider(annotation.entityIdClass())
                    .getProject((Long)joinPoint.getArgs()[entityIdIndex]);
        } else {
            throw new ProjectSecurityDataParsingException(
                    "Entity id class type must be " + Long.class.getName()
                    + " but currently is " + joinPoint.getArgs()[entityIdIndex]
                    .getClass().getName());
        }

        User user = null;
        if (joinPoint.getArgs()[userIndex] instanceof User) {
            user = (User)joinPoint.getArgs()[userIndex];
        } else {
            throw new ProjectSecurityDataParsingException(
                    "User class type must be " + User.class.getName()
                    + " but currently is " + joinPoint.getArgs()[userIndex].getClass().getName());
        }

        return new ProjectSecurityDto(project, user, annotation.securityLevel(),
                annotation.includePrivacyCheck());
    }

    private static class ProjectSecurityDto {
        private Project project;

        private User user;

        private ProjectRoleType roleRequired;

        private boolean includePrivacyCheck;

        public ProjectSecurityDto(Project project, User user,
                ProjectRoleType roleRequired, boolean includePrivacyCheck) {
            this.project = project;
            this.user = user;
            this.roleRequired = roleRequired;
            this.includePrivacyCheck = includePrivacyCheck;
        }
    }
}
