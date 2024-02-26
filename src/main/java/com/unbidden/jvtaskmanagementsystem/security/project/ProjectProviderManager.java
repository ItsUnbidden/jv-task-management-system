package com.unbidden.jvtaskmanagementsystem.security.project;

import com.unbidden.jvtaskmanagementsystem.model.Project;
import com.unbidden.jvtaskmanagementsystem.model.Task;
import com.unbidden.jvtaskmanagementsystem.security.project.provider.ProjectFromIdProvider;
import com.unbidden.jvtaskmanagementsystem.security.project.provider.ProjectFromTaskIdProvider;
import com.unbidden.jvtaskmanagementsystem.security.project.provider.ProjectProvider;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProjectProviderManager {
    private static final Map<Class<?>, ProjectProvider> PROVIDERS_MAP = new HashMap<>();

    public ProjectProviderManager(@Autowired List<ProjectProvider> providers) {
        for (ProjectProvider projectProvider : providers) {
            if (projectProvider.getClass().equals(ProjectFromIdProvider.class)) {
                PROVIDERS_MAP.put(Project.class, projectProvider);
            }
            if (projectProvider.getClass().equals(ProjectFromTaskIdProvider.class)) {
                PROVIDERS_MAP.put(Task.class, projectProvider);
            }
        }
    }

    public ProjectProvider getProvider(Class<?> clazz) {
        ProjectProvider projectProvider = PROVIDERS_MAP.get(clazz);
        if (projectProvider == null) {
            throw new IllegalArgumentException("There is no available ProjectProvider "
                    + "implementation for class " + clazz.getName());
        }
        return projectProvider;
    }
}
