package com.unbidden.jvtaskmanagementsystem.security.project.provider;

import org.springframework.stereotype.Component;

import com.unbidden.jvtaskmanagementsystem.model.Project;
import com.unbidden.jvtaskmanagementsystem.model.Subtask;
import com.unbidden.jvtaskmanagementsystem.util.EntityUtil;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProjectFromSubtaskProvider implements ProjectProvider {
    private final EntityUtil entityUtil;

    @Override
    public Project getProject(Long id) {
        final Subtask subtask = entityUtil.getSubtaskById(id);
        
        return subtask.getTask().getProject();
    }

    @Override
    public Class<?> getProviderClass() {
        return Subtask.class;
    }
}
