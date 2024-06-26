package com.unbidden.jvtaskmanagementsystem.security.project.provider;

import com.unbidden.jvtaskmanagementsystem.model.Label;
import com.unbidden.jvtaskmanagementsystem.model.Project;
import com.unbidden.jvtaskmanagementsystem.util.EntityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectFromLabelIdProvider implements ProjectProvider {
    private final EntityUtil entityUtil;

    @NonNull
    @Override
    public Project getProject(@NonNull Long id) {
        final Label label = entityUtil.getLabelById(id);
        return label.getProject();
    }

    @NonNull
    @Override
    public Class<?> getProviderClass() {
        return Label.class;
    }
}
