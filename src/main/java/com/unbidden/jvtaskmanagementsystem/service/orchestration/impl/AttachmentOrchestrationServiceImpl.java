package com.unbidden.jvtaskmanagementsystem.service.orchestration.impl;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.unbidden.jvtaskmanagementsystem.dto.attachment.AttachmentDto;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.ThirdPartyOperationResult.ThirdPartyOperationStatus;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.FileOperationResult;
import com.unbidden.jvtaskmanagementsystem.exception.ErrorType;
import com.unbidden.jvtaskmanagementsystem.exception.FileSizeLimitExceededException;
import com.unbidden.jvtaskmanagementsystem.exception.UnexpectedException;
import com.unbidden.jvtaskmanagementsystem.exception.thirdparty.dropbox.FileUploadFailedException;
import com.unbidden.jvtaskmanagementsystem.mapper.AttachmentMapper;
import com.unbidden.jvtaskmanagementsystem.model.Attachment;
import com.unbidden.jvtaskmanagementsystem.model.ProjectRole.ProjectRoleType;
import com.unbidden.jvtaskmanagementsystem.model.Task;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.security.project.ProjectSecurity;
import com.unbidden.jvtaskmanagementsystem.service.AttachmentService;
import com.unbidden.jvtaskmanagementsystem.service.DropboxService;
import com.unbidden.jvtaskmanagementsystem.service.orchestration.AttachmentOrchestrationService;
import com.unbidden.jvtaskmanagementsystem.util.EntityUtil;
import com.unbidden.jvtaskmanagementsystem.util.HttpClientUtil.HeaderNames;
import com.unbidden.jvtaskmanagementsystem.util.HttpClientUtil.HeaderValues;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttachmentOrchestrationServiceImpl implements AttachmentOrchestrationService {
    private static final long MAXIMUM_FILE_SIZE = 157_286_400L; 
    
    private final AttachmentService attachmentService;

    private final AttachmentMapper attachmentMapper;

    private final DropboxService dropboxService;

    private final EntityUtil entityUtil;

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, entityIdClass = Task.class)
    public Page<AttachmentDto> getAttachmentsForTask(@NonNull User user, @NonNull Long taskId,
            @NonNull Pageable pageable) {
        return attachmentService.getAttachmentsForTask(user, taskId, pageable)
                .map(attachmentMapper::toDto);
    }

    // TODO: need to finish this by dealing with the Dropbox result
    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, entityIdClass = Attachment.class)
    public void download(@NonNull User user, @NonNull HttpServletResponse response,
            @NonNull Long attachmentId) {
        final Attachment attachment = entityUtil.getAttachmentById(attachmentId);
        final User authorizedUser = (entityUtil.isManager(user)
                ? entityUtil.getProjectOwner(attachment.getTask().getProject()) : user);

        response.setContentType(HeaderValues.OCTET_STREAM);
        response.setHeader(HeaderNames.CONTENT_DISPOSITION, "attachment; filename="
                + attachment.getFilename());
        try {
            final FileOperationResult dropboxResult = dropboxService.downloadFile(
                authorizedUser, attachment.getDropboxId(), response.getOutputStream());
        } catch (IOException e) {
            throw new UnexpectedException("Exception occured during an "
                    + "attempt to get output stream.", ErrorType.INTERNAL);
        }
    }

    // TODO: need to finish this by dealing with the Dropbox result
    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, entityIdClass = Task.class)
    public AttachmentDto upload(@NonNull User user, @NonNull Long taskId,
            @NonNull MultipartFile file) {
        final Task task = entityUtil.getTaskById(taskId);
        final User authorizedUser = (entityUtil.isManager(user)
                ? entityUtil.getProjectOwner(task.getProject()) : user);

        final Attachment attachment = new Attachment();
        attachment.setTask(task);

        final String originalFileName = file.getOriginalFilename();
        if (originalFileName != null) {
            attachment.setFilename(originalFileName.replace(" ", "_"));
        }
        
        if (file.getSize() > MAXIMUM_FILE_SIZE) { 
            throw new FileSizeLimitExceededException("Maximum file size is 150 MiB. "
                    + "Current file size has exceeded this limit.",
                    ErrorType.ATTACHMENT_FILE_TOO_LARGE);
        }

        final FileOperationResult dropboxResult = dropboxService.uploadFileInTaskFolder(
                authorizedUser, task, file);
        if (!dropboxResult.getStatus().equals(ThirdPartyOperationStatus.SUCCESS)) {
            throw new FileUploadFailedException("Filed to upload the file.",
                    ErrorType.ATTACHMENT_UPLOAD_FAILURE, dropboxResult);
        }
        attachment.setDropboxId(dropboxResult.getMeta().getId());
        attachment.setUploadDate(LocalDateTime.now());
        return attachmentMapper.toDto(attachmentService.upload(attachment));
    }

    // TODO: need to finish this by dealing with the Dropbox result
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, entityIdClass = Attachment.class)
    public void delete(@NonNull User user, @NonNull Long attachmentId) {
        final Attachment attachment = entityUtil.getAttachmentById(attachmentId);
        final User authorizedUser = (entityUtil.isManager(user)
                ? entityUtil.getProjectOwner(attachment.getTask().getProject()) : user);

        attachmentService.delete(attachmentId);
        dropboxService.deleteFile(authorizedUser, attachment.getDropboxId());
    }

}
