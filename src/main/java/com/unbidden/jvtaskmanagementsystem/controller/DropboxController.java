package com.unbidden.jvtaskmanagementsystem.controller;

import com.unbidden.jvtaskmanagementsystem.dto.internal.DropboxTestResponseDto;
import com.unbidden.jvtaskmanagementsystem.model.ClientRegistration;
import com.unbidden.jvtaskmanagementsystem.model.OAuth2AuthorizedClient;
import com.unbidden.jvtaskmanagementsystem.repository.oauth2.ClientRegistrationRepository;
import com.unbidden.jvtaskmanagementsystem.service.client.DropboxClient;
import com.unbidden.jvtaskmanagementsystem.service.oauth2.OAuth2Service;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dropbox")
@SuppressWarnings("null")
public class DropboxController {
    private final OAuth2Service oauthService;

    private final DropboxClient dropboxClient;

    private final ClientRegistrationRepository clientRegistrationRepository;

    @GetMapping("/test")
    public DropboxTestResponseDto test(Authentication authentication,
            HttpServletRequest request, HttpServletResponse response) {
        ClientRegistration clientRegistration = 
                clientRegistrationRepository.findByClientName("dropbox").get();
        OAuth2AuthorizedClient authorizedClient = oauthService.loadAuthorizedClient(
                authentication, request, response, clientRegistration);

        return (authorizedClient != null) ? dropboxClient.test(authorizedClient) : null;
    }
}
