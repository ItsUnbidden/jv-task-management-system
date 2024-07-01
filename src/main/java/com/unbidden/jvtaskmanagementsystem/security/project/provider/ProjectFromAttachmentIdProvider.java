package com.unbidden.jvtaskmanagementsystem.security.project.provider;

import com.unbidden.jvtaskmanagementsystem.model.Attachment;
import com.unbidden.jvtaskmanagementsystem.model.Project;
import com.unbidden.jvtaskmanagementsystem.util.EntityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectFromAttachmentIdProvider implements ProjectProvider {
    private final EntityUtil entityUtil;

    @NonNull
    @Override
    public Project getProject(@NonNull Long id) {
        return entityUtil.getAttachmentById(id).getTask().getProject();
    }

    @NonNull
    @Override
    public Class<?> getProviderClass() {
        return Attachment.class;
    }
}
