package com.unbidden.jvtaskmanagementsystem.service.orchestration;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;

import com.unbidden.jvtaskmanagementsystem.dto.attachment.AttachmentDto;
import com.unbidden.jvtaskmanagementsystem.model.User;

import jakarta.servlet.http.HttpServletResponse;

public interface AttachmentOrchestrationService {
    @NonNull
    Page<AttachmentDto> getAttachmentsForTask(@NonNull User user, @NonNull Long taskId,
            @NonNull Pageable pageable);

    @NonNull
    void download(@NonNull User user, @NonNull HttpServletResponse response,
            @NonNull Long attachmentId);

    @NonNull
    AttachmentDto upload(@NonNull User user, @NonNull Long taskId, @NonNull MultipartFile file);

    void delete(@NonNull User user, @NonNull Long attachmentId);
}
