package com.unbidden.jvtaskmanagementsystem.security.project;

import org.aspectj.lang.JoinPoint;

/**
 * This class contains a single {@link #parse} method that parses raw data from {@link JoinPoint}
 * into {@link ProjectSecurityDto}.
 * @author Unbidden
 */
public interface ProjectSecurityDataParser {
    /**
     * Parses {@link JoinPoint} raw method data into a convinient to access
     * {@link ProjectSecurityDto} object. That includes guessing some parameter
     * names if they are not provided in {@link ProjectSecurity} annotation.
     * @param data from the method
     * @return {@link ProjectSecurityDto} object with parsed data
     */
    ProjectSecurityDto parse(JoinPoint data);
}
