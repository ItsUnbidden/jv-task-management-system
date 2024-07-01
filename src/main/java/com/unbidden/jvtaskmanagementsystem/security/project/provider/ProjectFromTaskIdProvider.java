package com.unbidden.jvtaskmanagementsystem.security.project.provider;

import com.unbidden.jvtaskmanagementsystem.model.Project;
import com.unbidden.jvtaskmanagementsystem.model.Task;
import com.unbidden.jvtaskmanagementsystem.util.EntityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectFromTaskIdProvider implements ProjectProvider {
    private final EntityUtil entityUtil;

    @NonNull
    @Override
    public Project getProject(@NonNull Long id) {
        final Task taskById = entityUtil.getTaskById(id);
        return taskById.getProject();
    }

    @NonNull
    @Override
    public Class<?> getProviderClass() {
        return Task.class;
    }
}
