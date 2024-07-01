package com.unbidden.jvtaskmanagementsystem.security.project.provider;

import com.unbidden.jvtaskmanagementsystem.model.Project;
import org.springframework.lang.NonNull;

public interface ProjectProvider {
    @NonNull
    Project getProject(@NonNull Long id);

    @NonNull
    Class<?> getProviderClass();
}
