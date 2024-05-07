package com.unbidden.jvtaskmanagementsystem.security.project;

import com.unbidden.jvtaskmanagementsystem.model.ProjectRole;
import com.unbidden.jvtaskmanagementsystem.util.EntityUtil;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ProjectSecurityAspect {
    private static final Logger LOGGER = LogManager.getLogger(ProjectSecurityAspect.class);

    private final EntityUtil entityUtil;

    private final ProjectSecurityDataParser dataParser;

    @Before("execution(public * com.unbidden.jvtaskmanagementsystem.service.impl..*(..)) && "
            + "@annotation(com.unbidden.jvtaskmanagementsystem.security.project.ProjectSecurity)")
    public void projectAccessAdvice(JoinPoint joinPoint) {
        LOGGER.info("Project security aspect commencing...");
        ProjectSecurityDto dataFromJoinPoint = dataParser.parse(joinPoint);

        if (dataFromJoinPoint.isBypassIfPublic()
                && !dataFromJoinPoint.getProject().isPrivate()) {
            LOGGER.info("Project " + dataFromJoinPoint.getProject().getId()
                    + " is public. Security is unnecessary.");
            return;
        }
        if (entityUtil.isManager(dataFromJoinPoint.getUser())) {
            LOGGER.warn("User " + dataFromJoinPoint.getUser().getId()
                    + " is an application manager. Security bypassed.");
            return;
        }

        checkUserAccess(dataFromJoinPoint);
        LOGGER.info("Access granted to user " + dataFromJoinPoint.getUser().getId()
                + " for project " + dataFromJoinPoint.getProject().getId());
    }

    private void checkUserAccess(ProjectSecurityDto dto) {
        final ProjectRole projectRole = entityUtil
                .getProjectRoleByProjectIdAndUserId(dto.getProject().getId(),
                dto.getUser().getId());

        if (projectRole.getRoleType().compareTo(dto.getRoleRequired()) > 0) {
            throw new AccessDeniedException("User does not have required project role <"
                    + dto.getRoleRequired() + "> to access this resource.");
        }
    }
}
