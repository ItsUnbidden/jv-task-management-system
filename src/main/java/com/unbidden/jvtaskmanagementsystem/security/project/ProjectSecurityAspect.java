package com.unbidden.jvtaskmanagementsystem.security.project;

import com.unbidden.jvtaskmanagementsystem.model.ProjectRole;
import com.unbidden.jvtaskmanagementsystem.service.util.EntityUtil;
import lombok.RequiredArgsConstructor;
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
    private final EntityUtil entityUtil;

    private final ProjectSecurityDataParser dataParser;

    @Before("execution(public * com.unbidden.jvtaskmanagementsystem.service.impl..*(..)) && "
            + "@annotation(com.unbidden.jvtaskmanagementsystem.security.project.ProjectSecurity)")
    public void projectAccessAdvice(JoinPoint joinPoint) {
        ProjectSecurityDto dataFromJoinPoint = dataParser.parse(joinPoint);

        if (dataFromJoinPoint.isIncludePrivacyCheck()
                && !dataFromJoinPoint.getProject().isPrivate()) {
            return;
        }
        if (entityUtil.isManager(dataFromJoinPoint.getUser())) {
            return;
        }

        checkUserAccess(dataFromJoinPoint);
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
