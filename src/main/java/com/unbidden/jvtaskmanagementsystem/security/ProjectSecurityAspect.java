package com.unbidden.jvtaskmanagementsystem.security;

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

    @Before("execution(public * com.unbidden.jvtaskmanagementsystem."
            + "service.impl.ProjectServiceImpl.findProjectById(..))")
    public void getProjectByIdAccessAdvice(JoinPoint joinPoint) {
        ProjectSecurityDto dataFromJoinPoint = parseDataFromJoinPoint(joinPoint);

        System.out.println("Get project id aspect is running...");
        if (entityUtil.isManager(dataFromJoinPoint.user) 
                || !dataFromJoinPoint.project.isPrivate()) {
            return;
        }

        checkUserAccess(dataFromJoinPoint);
    }

    @Before("execution(public * com.unbidden.jvtaskmanagementsystem.service.impl..*(..)) "
            + "&& @annotation(com.unbidden.jvtaskmanagementsystem.security.ProjectSecurity) "
            + "&& ! execution(public * com.unbidden.jvtaskmanagementsystem."
            + "service.impl.ProjectServiceImpl.findProjectById(..))")
    public void userAccessInProjectAdvice(JoinPoint joinPoint) {
        ProjectSecurityDto dataFromJoinPoint = parseDataFromJoinPoint(joinPoint);

        System.out.println("General project security aspect is running...");
        if (entityUtil.isManager(dataFromJoinPoint.user)) {
            return;
        }

        checkUserAccess(dataFromJoinPoint);
    }

    private void checkUserAccess(ProjectSecurityDto dto) {
        ProjectRole projectRole = entityUtil
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
        int projectIdIndex = -1;
        int userIndex = -1;

        for (int i = 0; i < parameterNames.length; i++) {
            if (parameterNames[i].equals(annotation.projectIdParamName())) {
                projectIdIndex = i;
            }
            if (parameterNames[i].equals(annotation.userParamName())) {
                userIndex = i;
            }
        }

        if (projectIdIndex == -1) {
            throw new ProjectSecurityDataParsingException("Method " + signature.getName() 
                    + "does not have a parameter with name " 
                    + annotation.projectIdParamName() + '.');
        }
        if (userIndex == -1) {
            throw new ProjectSecurityDataParsingException("Method " + signature.getName() 
                    + "does not have a parameter with name " 
                    + annotation.userParamName() + '.');
        }

        Long projectId = null;
        if (joinPoint.getArgs()[projectIdIndex] instanceof Long) {
            projectId = (Long)joinPoint.getArgs()[projectIdIndex];
        } else {
            throw new ProjectSecurityDataParsingException(
                    "Project id class type must be " + Long.class.getName()
                    + " but currently is " + joinPoint.getArgs()[projectIdIndex]
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

        Project project = entityUtil.getProjectById(projectId);
        return new ProjectSecurityDto(project, user, annotation.securityLevel());
    }

    private static class ProjectSecurityDto {
        private Project project;

        private User user;

        private ProjectRoleType roleRequired;

        public ProjectSecurityDto(Project project, User user, ProjectRoleType roleRequired) {
            this.project = project;
            this.user = user;
            this.roleRequired = roleRequired;
        }
    }
}
