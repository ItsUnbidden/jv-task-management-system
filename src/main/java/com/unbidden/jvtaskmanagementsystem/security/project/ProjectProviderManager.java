package com.unbidden.jvtaskmanagementsystem.security.project;

import com.unbidden.jvtaskmanagementsystem.security.project.provider.ProjectProvider;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class ProjectProviderManager {
    private static final Map<Class<?>, ProjectProvider> PROVIDERS_MAP = new HashMap<>();

    public ProjectProviderManager(@Autowired List<ProjectProvider> providers) {
        for (ProjectProvider projectProvider : providers) {
            PROVIDERS_MAP.put(projectProvider.getProviderClass(), projectProvider);
        }
    }

    @NonNull
    public ProjectProvider getProvider(@NonNull Class<?> clazz) {
        ProjectProvider projectProvider = PROVIDERS_MAP.get(clazz);
        if (projectProvider == null) {
            throw new IllegalArgumentException("There is no available ProjectProvider "
                    + "implementation for class " + clazz.getName());
        }
        return projectProvider;
    }
}
