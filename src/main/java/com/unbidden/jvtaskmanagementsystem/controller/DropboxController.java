package com.unbidden.jvtaskmanagementsystem.controller;

import com.dropbox.core.DbxApiException;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.check.EchoResult;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.service.DropboxService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dropbox")
@SuppressWarnings("null")
public class DropboxController {
    private final DropboxService dropboxService;

    @GetMapping("/test")
    public EchoResult test(Authentication authentication)
            throws DbxApiException, DbxException {
        return dropboxService.testDropboxUserConnection((User)authentication.getPrincipal());
    }

    @DeleteMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(Authentication authentication,
            HttpServletRequest request, HttpServletResponse response)
            throws DbxApiException, DbxException {
        dropboxService.logout((User)authentication.getPrincipal());
    }
}
