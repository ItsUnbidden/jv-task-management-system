package com.unbidden.jvtaskmanagementsystem.security;

import com.unbidden.jvtaskmanagementsystem.dto.auth.LoginRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.auth.LoginResponseDto;
import com.unbidden.jvtaskmanagementsystem.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AuthenticationService {
    private static final Logger LOGGER = LogManager.getLogger(AuthenticationService.class);

    private final AuthenticationManager authManager;

    private final JwtUtil jwtUtil;

    public LoginResponseDto authenticate(@NonNull LoginRequestDto requestDto) {
        LOGGER.info("User " + requestDto.getUsername() + " is trying to authenticate.");
        final Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                requestDto.getUsername(), requestDto.getPassword()));
        LOGGER.info("User " + requestDto.getUsername() + " has been authenticatied.");
        return new LoginResponseDto(jwtUtil.generateToken(authentication.getName()));
    }
}
