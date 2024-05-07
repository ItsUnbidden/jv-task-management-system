package com.unbidden.jvtaskmanagementsystem.service.impl;

import com.dropbox.core.v2.files.FileMetadata;
import com.unbidden.jvtaskmanagementsystem.dto.attachment.AttachmentDto;
import com.unbidden.jvtaskmanagementsystem.mapper.AttachmentMapper;
import com.unbidden.jvtaskmanagementsystem.model.Attachment;
import com.unbidden.jvtaskmanagementsystem.model.ProjectRole.ProjectRoleType;
import com.unbidden.jvtaskmanagementsystem.model.Task;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.repository.AttachmentRepository;
import com.unbidden.jvtaskmanagementsystem.security.project.ProjectSecurity;
import com.unbidden.jvtaskmanagementsystem.service.AttachmentService;
import com.unbidden.jvtaskmanagementsystem.service.DropboxService;
import com.unbidden.jvtaskmanagementsystem.util.EntityUtil;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {
    private final AttachmentRepository attachmentRepository;

    private final EntityUtil entityUtil;

    private final AttachmentMapper attachmentMapper;

    private final DropboxService dropboxService;

    @Override
    @NonNull
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, entityIdClass = Task.class,
            bypassIfPublic = true)
    public List<AttachmentDto> getAttachmentsForTask(@NonNull User user, @NonNull Long taskId,
            Pageable pageable) {
        return attachmentRepository.findByTaskId(taskId, pageable).stream()
                .map(attachmentMapper::toDto)
                .toList();
    }

    @Override
    @NonNull
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR,
            entityIdClass = Attachment.class, bypassIfPublic = true)
    public void download(@NonNull User user, @NonNull HttpServletResponse response,
            @NonNull Long attachmentId) {
        final Attachment attachment = entityUtil.getAttachmentById(attachmentId);

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename="
                + attachment.getFilename());
        try {
            dropboxService.downloadFile(user, attachment.getDropboxId(),
                    response.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException("Exception occured during an "
                    + "attempt to get output stream.");
        }
    }

    @Override
    @NonNull
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, entityIdClass = Task.class)
    public AttachmentDto upload(@NonNull User user, @NonNull Long taskId,
            @NonNull MultipartFile file) {
        final Task task = entityUtil.getTaskById(taskId);

        Attachment attachment = new Attachment();
        attachment.setTask(task);
        attachment.setFilename(file.getOriginalFilename());

        FileMetadata meta = dropboxService.uploadFileInTaskFolder(user, task, file);
        attachment.setDropboxId(meta.getId());
        attachment.setUploadDate(LocalDateTime.now());
        return attachmentMapper.toDto(attachmentRepository.save(attachment));
    }
}
