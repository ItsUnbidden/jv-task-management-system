package com.unbidden.jvtaskmanagementsystem.security.project;

import com.unbidden.jvtaskmanagementsystem.model.Project;
import com.unbidden.jvtaskmanagementsystem.model.ProjectRole;
import com.unbidden.jvtaskmanagementsystem.model.ProjectRole.ProjectRoleType;
import com.unbidden.jvtaskmanagementsystem.model.User;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation applies project security to a service method. That includes:
 * <ul>
 *  <li>Testing whether {@link User} is a member of a {@link Project}</li>
 *  <li>What {@link ProjectRole} is required to access a method</li>
 *  <li>Does privacy of a project matter for method access</li>
 * </ul>
 * <p>The method in question must have several specific parameters 
 * in order for the aspect to fetch the necessary data for access checking:
 * <ul>
 *  <li>{@code Long id} — id of an entity from which project can be fetched</li>
 *  <li>{@code User user} — user that is trying to access the method</li>
 * </ul>
 * @param securityLevel — required role for method access
 * @param entityIdParamName — name of the entity id parameter. By default equals {@code projectId}
 * @param entityIdClass — class of the entity from which project can be fetched.
 * By default equals {@code Project.class}
 * @param userParamName — name of the user parameter. By default equals {@code user}
 * @param bypassIfPublic — whether check for project privacy matters. 
 * Mainly required when retrieving entities from the database. By default is {@code false}.
 * @author Unbidden
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ProjectSecurity {
    ProjectRoleType securityLevel();

    String entityIdParamName() default "projectId";

    Class<?> entityIdClass() default Project.class;

    String userParamName() default "user";

    boolean bypassIfPublic() default false;
}
