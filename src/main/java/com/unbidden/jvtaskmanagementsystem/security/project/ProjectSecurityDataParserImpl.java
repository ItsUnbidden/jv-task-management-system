package com.unbidden.jvtaskmanagementsystem.security.project;

import com.unbidden.jvtaskmanagementsystem.exception.ProjectSecurityDataParsingException;
import com.unbidden.jvtaskmanagementsystem.model.Project;
import com.unbidden.jvtaskmanagementsystem.model.User;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class ProjectSecurityDataParserImpl implements ProjectSecurityDataParser {
    private static final Logger LOGGER =
            LogManager.getLogger(ProjectSecurityDataParserImpl.class);

    private final ProjectProviderManager projectProviderManager;

    @NonNull
    @Override
    public ProjectSecurityDto parse(@NonNull JoinPoint data) {
        MethodSignature signature = (MethodSignature)data.getSignature();
        ProjectSecurity annotation = signature.getMethod().getAnnotation(ProjectSecurity.class);
        String[] parameterNames = signature.getParameterNames();
        int entityIdIndex = -1;
        int userIndex = -1;

        LOGGER.info("Parsing of join point from security aspect is commencing.");
        final Class<?> entityIdClass = annotation.entityIdClass();
        final String entityParamName = (annotation.entityIdParamName().isBlank())
                ? StringUtils.uncapitalize(annotation.entityIdClass().getSimpleName()) + "Id"
                : annotation.entityIdParamName();
        final String userParamName = annotation.userParamName();

        for (int i = 0; i < parameterNames.length; i++) {
            if (parameterNames[i].equals(entityParamName)) {
                entityIdIndex = i;
            }
            if (userParamName.isBlank()) {
                if (parameterNames[i].equals("user")
                        || parameterNames[i].equals("authentication")) {
                    userIndex = i;
                }
            } else {
                if (parameterNames[i].equals(userParamName)) {
                    userIndex = i;
                }
            } 
        }

        if (entityIdIndex == -1) {
            throw new ProjectSecurityDataParsingException("Method " + signature.getName() 
                    + " does not have a parameter with name " 
                    + entityParamName + '.');
        }
        if (userIndex == -1) {
            throw new ProjectSecurityDataParsingException("Method " + signature.getName() 
                    + " does not have a parameter with name " 
                    + userParamName + '.');
        }
        
        Project project = null;
        Object entityArg = data.getArgs()[entityIdIndex];
        if (entityArg instanceof Long) {
            project = projectProviderManager.getProvider(entityIdClass)
                    .getProject((Long)entityArg);
            LOGGER.info("Project " + project.getId() + " has been aquired.");
        } else {
            throw new ProjectSecurityDataParsingException(
                    "Entity id class type must be " + Long.class.getName()
                    + " but currently is " + entityArg.getClass().getName());
        }

        User user = null;
        Object userArg = data.getArgs()[userIndex];
        if (userArg instanceof User) {
            user = (User)userArg;
            LOGGER.info("User " + user.getId() + " has been aquired.");
        } else if (userArg instanceof Authentication) {
            user = (User)((Authentication)userArg).getPrincipal();
            LOGGER.info("User " + user.getId() + " has been aquired from authentication object.");
        } else {
            throw new ProjectSecurityDataParsingException(
                    "User class type must be either " + User.class.getName()
                    + " or " + Authentication.class.getName() + " but currently is "
                    + data.getArgs()[userIndex].getClass().getName());
        }

        LOGGER.info("Data successfuly parsed. User id: " + user.getId()
                + "; project id " + project.getId());
        return new ProjectSecurityDto(project, user, annotation.securityLevel(),
                annotation.bypassIfPublic());
    }
}
