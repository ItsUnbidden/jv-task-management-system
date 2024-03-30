package com.unbidden.jvtaskmanagementsystem.controller;

import com.unbidden.jvtaskmanagementsystem.dto.attachment.AttachmentDto;
import com.unbidden.jvtaskmanagementsystem.model.ClientRegistration;
import com.unbidden.jvtaskmanagementsystem.model.OAuth2AuthorizedClient;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.repository.oauth2.ClientRegistrationRepository;
import com.unbidden.jvtaskmanagementsystem.service.AttachmentService;
import com.unbidden.jvtaskmanagementsystem.service.oauth2.OAuth2Service;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/attachments")
@SuppressWarnings("null")
@RequiredArgsConstructor
public class AttachmentController {
    private final AttachmentService attachmentService;

    private final ClientRegistrationRepository clientRegistrationRepository;

    private final OAuth2Service oauthService;

    @GetMapping("/tasks/{taskId}")
    public List<AttachmentDto> getAvailableAttachmentsForTask(Authentication authentication,
            @PathVariable Long taskId, Pageable pageable) {
        return attachmentService.getAttachmentsForTask((User)authentication.getPrincipal(),
                taskId);
    }
    

    @PostMapping("/tasks/{taskId}")
    public AttachmentDto upload(Authentication authentication, HttpServletRequest request,
            HttpServletResponse response, @PathVariable Long taskId, @RequestBody byte[] data) {
        ClientRegistration clientRegistration = 
                clientRegistrationRepository.findByClientName("dropbox").get();
        OAuth2AuthorizedClient authorizedClient = oauthService.loadAuthorizedClient(
                authentication, request, response, clientRegistration);
        return (authorizedClient != null) ? attachmentService.upload(
                (User)authentication.getPrincipal(), taskId, authorizedClient, data) : null;
    }
    
}
