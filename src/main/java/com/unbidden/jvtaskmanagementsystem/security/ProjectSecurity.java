package com.unbidden.jvtaskmanagementsystem.security;

import com.unbidden.jvtaskmanagementsystem.model.ProjectRole.ProjectRoleType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ProjectSecurity {
    ProjectRoleType securityLevel();

    String projectIdParamName() default "projectId";

    String userParamName() default "user";
}
