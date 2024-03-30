package com.unbidden.jvtaskmanagementsystem.controller;

import com.unbidden.jvtaskmanagementsystem.exception.EntityNotFoundException;
import com.unbidden.jvtaskmanagementsystem.model.ClientRegistration;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.repository.oauth2.ClientRegistrationRepository;
import com.unbidden.jvtaskmanagementsystem.service.oauth2.OAuth2Service;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@SuppressWarnings("null")
@RequestMapping("/oauth2/connect")
public class OAuth2Controller {
    private static final String DROPBOX_CLIENT_NAME = "dropbox";

    private final ClientRegistrationRepository clientRegistrationRepository;

    private final OAuth2Service oauth2Service;

    @GetMapping("/dropbox")
    public void initiateDropboxAuthorization(Authentication authentication, 
            HttpServletResponse response, String origin) {
        ClientRegistration clientRegistration = clientRegistrationRepository
                .findByClientName(DROPBOX_CLIENT_NAME)
                .orElseThrow(() -> new EntityNotFoundException(
                "There is no client registered with name " + DROPBOX_CLIENT_NAME));
        oauth2Service.authorize((User)authentication.getPrincipal(), response, 
                clientRegistration, origin);
    }
    
    @GetMapping("/code")
    public void callback(HttpServletResponse response,
            @RequestParam String code, @RequestParam String state,
            String error, String errorDescription) {
        oauth2Service.callback(response, code, state, error, errorDescription);
    }
}
