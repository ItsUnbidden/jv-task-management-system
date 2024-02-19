package com.unbidden.jvtaskmanagementsystem.security;

import com.unbidden.jvtaskmanagementsystem.dto.auth.LoginRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.auth.LoginResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AuthenticationService {
    private final AuthenticationManager authManager;

    private final JwtUtil jwtUtil;

    public LoginResponseDto authenticate(@NonNull LoginRequestDto requestDto) {
        final Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                requestDto.getUsername(), requestDto.getPassword()));
                
        return new LoginResponseDto(jwtUtil.generateToken(authentication.getName()));
    }
}
