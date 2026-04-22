package com.unbidden.jvtaskmanagementsystem.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unbidden.jvtaskmanagementsystem.model.Attachment;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.repository.AttachmentRepository;
import com.unbidden.jvtaskmanagementsystem.service.AttachmentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {
    private final AttachmentRepository attachmentRepository;

    @NonNull
    @Override
    @Transactional(readOnly = true)
    public Page<Attachment> getAttachmentsForTask(@NonNull User user, @NonNull Long taskId,
            Pageable pageable) {
        return attachmentRepository.findByTaskId(taskId, pageable);
    }

    @NonNull
    @Override
    @Transactional
    public Attachment upload(@NonNull Attachment attachment) {
        return attachmentRepository.save(attachment);
    }

    @Override
    @Transactional
    public void delete(@NonNull Long attachmentId) {
        attachmentRepository.deleteById(attachmentId);
    }
}
