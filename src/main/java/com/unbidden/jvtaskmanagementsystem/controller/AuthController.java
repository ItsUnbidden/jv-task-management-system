package com.unbidden.jvtaskmanagementsystem.controller;

import com.unbidden.jvtaskmanagementsystem.dto.auth.LoginRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.auth.LoginResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.auth.RegistrationRequest;
import com.unbidden.jvtaskmanagementsystem.dto.user.UserResponseDto;
import com.unbidden.jvtaskmanagementsystem.exception.RegistrationException;
import com.unbidden.jvtaskmanagementsystem.security.AuthenticationService;
import com.unbidden.jvtaskmanagementsystem.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    private final AuthenticationService authService;

    @PostMapping("/register")
    public UserResponseDto register(@Valid @RequestBody RegistrationRequest request) 
            throws RegistrationException {
        return userService.register(request);
    }

    @PostMapping("/login")
    public LoginResponseDto login(@Valid @RequestBody LoginRequestDto request) {
        return authService.authenticate(request);
    }
}
