package com.unbidden.jvtaskmanagementsystem.service.impl;

import com.unbidden.jvtaskmanagementsystem.dto.attachment.AttachmentDto;
import com.unbidden.jvtaskmanagementsystem.mapper.AttachmentMapper;
import com.unbidden.jvtaskmanagementsystem.model.OAuth2AuthorizedClient;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.repository.AttachmentRepository;
import com.unbidden.jvtaskmanagementsystem.service.AttachmentService;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {
    private final AttachmentRepository attachmentRepository;

    private final AttachmentMapper attachmentMapper;

    @Override
    @NonNull
    public List<AttachmentDto> getAttachmentsForTask(@NonNull User user, @NonNull Long taskId,
            Pageable pageable) {
        return attachmentRepository.findByTaskId(taskId, pageable).stream()
                .map(attachmentMapper::toDto).
                toList();
    }

    @Override
    @NonNull
    public byte[] download(@NonNull User user, @NonNull Long attachmentId,
            @NonNull OAuth2AuthorizedClient authorizedClient) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'download'");
    }

    @Override
    @NonNull
    public AttachmentDto upload(@NonNull User user, @NonNull Long taskId,
            @NonNull OAuth2AuthorizedClient authorizedClient, byte[] data) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'upload'");
    }

}
