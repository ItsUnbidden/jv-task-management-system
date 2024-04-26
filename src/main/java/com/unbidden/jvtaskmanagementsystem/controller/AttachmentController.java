package com.unbidden.jvtaskmanagementsystem.controller;

import com.unbidden.jvtaskmanagementsystem.dto.attachment.AttachmentDto;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.service.AttachmentService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/attachments")
@SuppressWarnings("null")
@RequiredArgsConstructor
public class AttachmentController {
    private final AttachmentService attachmentService;

    @GetMapping("/tasks/{taskId}")
    public List<AttachmentDto> getAvailableAttachmentsForTask(Authentication authentication,
            @PathVariable Long taskId, Pageable pageable) {
        return attachmentService.getAttachmentsForTask((User)authentication.getPrincipal(),
                taskId, pageable);
    }
    
    @PostMapping("/tasks/{taskId}")
    public AttachmentDto upload(Authentication authentication, @PathVariable Long taskId, 
            @RequestParam("file") MultipartFile file) throws IOException {
        return attachmentService.upload((User)authentication.getPrincipal(), taskId, file);
    }
    
    @GetMapping("/{attachmentId}")
    public void download(Authentication authentication, HttpServletResponse response,
            @PathVariable Long attachmentId) {
        attachmentService.download((User)authentication.getPrincipal(), response, attachmentId);
    }
}
