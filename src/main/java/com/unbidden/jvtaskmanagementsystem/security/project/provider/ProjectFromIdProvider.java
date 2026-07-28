package com.unbidden.jvtaskmanagementsystem.security.project.provider;

import com.unbidden.jvtaskmanagementsystem.model.Project;
import com.unbidden.jvtaskmanagementsystem.util.EntityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ProjectFromIdProvider implements ProjectProvider {
    private final EntityUtil entityUtil;

    @NonNull
    @Override
    @Transactional(readOnly = true)
    public Project getProject(@NonNull Long id) {
        return entityUtil.getProjectById(id);
    }

    @NonNull
    @Override
    public Class<?> getProviderClass() {
        return Project.class;
    }
}
