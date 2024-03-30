package com.unbidden.jvtaskmanagementsystem.service;

import com.unbidden.jvtaskmanagementsystem.dto.attachment.AttachmentDto;
import com.unbidden.jvtaskmanagementsystem.model.OAuth2AuthorizedClient;
import com.unbidden.jvtaskmanagementsystem.model.User;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;

public interface AttachmentService {
    @NonNull
    List<AttachmentDto> getAttachmentsForTask(@NonNull User user, @NonNull Long taskId,
            Pageable pageable);

    @NonNull
    byte[] download(@NonNull User user, @NonNull Long attachmentId,
            @NonNull OAuth2AuthorizedClient authorizedClient);

    @NonNull
    AttachmentDto upload(@NonNull User user, @NonNull Long taskId, 
            @NonNull OAuth2AuthorizedClient authorizedClient, byte[] data);
}
