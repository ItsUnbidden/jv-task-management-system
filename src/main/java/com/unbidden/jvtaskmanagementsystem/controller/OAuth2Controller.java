package com.unbidden.jvtaskmanagementsystem.controller;

import com.unbidden.jvtaskmanagementsystem.dto.oauth2.OAuth2SuccessResponse;
import com.unbidden.jvtaskmanagementsystem.model.ClientRegistration;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.service.oauth2.OAuth2Service;
import com.unbidden.jvtaskmanagementsystem.util.EntityUtil;
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
    private final EntityUtil entityUtil;

    private final OAuth2Service oauth2Service;

    @GetMapping("/dropbox")
    public void initiateDropboxAuthorization(Authentication authentication, 
            HttpServletResponse response) {
        ClientRegistration clientRegistration = entityUtil.getClientRegistrationByName("dropbox");
        oauth2Service.authorize((User)authentication.getPrincipal(), response, 
                clientRegistration);
    }

    @GetMapping("/google")
    public void initiateGoogleAuthorization(Authentication authentication, 
            HttpServletResponse response) {
        ClientRegistration clientRegistration = entityUtil.getClientRegistrationByName("google");
        oauth2Service.authorize((User)authentication.getPrincipal(), response, 
                clientRegistration);
    }
    
    @GetMapping("/code")
    public OAuth2SuccessResponse callback(HttpServletResponse response,
            @RequestParam String code, @RequestParam String state,
            String error, String errorDescription) {
        return oauth2Service.callback(response, code, state, error, errorDescription);
    }
}
