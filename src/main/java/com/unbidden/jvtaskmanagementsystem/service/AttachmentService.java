package com.unbidden.jvtaskmanagementsystem.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;

import com.unbidden.jvtaskmanagementsystem.model.Attachment;
import com.unbidden.jvtaskmanagementsystem.model.User;

public interface AttachmentService {
    @NonNull
    Page<Attachment> getAttachmentsForTask(@NonNull User user, @NonNull Long taskId,
            Pageable pageable);

    @NonNull
    Attachment upload(@NonNull Attachment attachment);

    void delete(@NonNull Long attachmentId);
}
