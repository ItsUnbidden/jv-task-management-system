package com.unbidden.jvtaskmanagementsystem.service.impl;

import com.dropbox.core.v2.files.FileMetadata;
import com.unbidden.jvtaskmanagementsystem.dto.attachment.AttachmentDto;
import com.unbidden.jvtaskmanagementsystem.exception.FileSizeLimitExceededException;
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
    private static final long MAXIMUM_FILE_SIZE = 157_300_000L; 

    private final AttachmentRepository attachmentRepository;

    private final EntityUtil entityUtil;

    private final AttachmentMapper attachmentMapper;

    private final DropboxService dropboxService;

    @Override
    @NonNull
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, entityIdClass = Task.class)
    public List<AttachmentDto> getAttachmentsForTask(@NonNull User user, @NonNull Long taskId,
            Pageable pageable) {
        return attachmentRepository.findByTaskId(taskId, pageable).stream()
                .map(attachmentMapper::toDto)
                .toList();
    }

    @Override
    @NonNull
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR,
            entityIdClass = Attachment.class)
    public void download(@NonNull User user, @NonNull HttpServletResponse response,
            @NonNull Long attachmentId) {
        final Attachment attachment = entityUtil.getAttachmentById(attachmentId);
        final User authorizedUser = (entityUtil.isManager(user)
                ? entityUtil.getProjectOwner(attachment.getTask().getProject()) : user);

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename="
                + attachment.getFilename());
        try {
            dropboxService.downloadFile(authorizedUser, attachment.getDropboxId(),
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
        final User authorizedUser = (entityUtil.isManager(user)
                ? entityUtil.getProjectOwner(task.getProject()) : user);

        Attachment attachment = new Attachment();
        attachment.setTask(task);

        if (file.getOriginalFilename() != null) {
            attachment.setFilename(file.getOriginalFilename().replace(" ", "_"));
        }
        
        if (file.getSize() > MAXIMUM_FILE_SIZE) {
            throw new FileSizeLimitExceededException("Maximum file size is 150 MiB. "
                    + "Current file size has exceeded this limit.");
        }

        FileMetadata meta = dropboxService.uploadFileInTaskFolder(authorizedUser, task, file);
        attachment.setDropboxId(meta.getId());
        attachment.setUploadDate(LocalDateTime.now());
        return attachmentMapper.toDto(attachmentRepository.save(attachment));
    }
}
