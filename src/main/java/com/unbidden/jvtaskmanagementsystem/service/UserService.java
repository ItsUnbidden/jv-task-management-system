package com.unbidden.jvtaskmanagementsystem.service;

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;

import com.unbidden.jvtaskmanagementsystem.dto.auth.LoginRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.auth.RegistrationRequest;
import com.unbidden.jvtaskmanagementsystem.dto.user.DeleteUserResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.user.UserResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.user.UserUpdateDetailsRequestDto;
import com.unbidden.jvtaskmanagementsystem.exception.RegistrationException;
import com.unbidden.jvtaskmanagementsystem.model.Role;
import com.unbidden.jvtaskmanagementsystem.model.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface UserService {
    @NonNull
    UserResponseDto register(@NonNull RegistrationRequest request) throws RegistrationException;

    @NonNull
    UserResponseDto findCurrentUser(@NonNull User user);

    @NonNull
    UserResponseDto updateRoles(@NonNull Long id, @NonNull Set<Role> roles);

    @NonNull
    UserResponseDto updateUserDetails(@NonNull User user,
            @NonNull UserUpdateDetailsRequestDto requestDto);

    @NonNull
    Page<UserResponseDto> searchByUsername(@NonNull String username, @NonNull Pageable pageable);

    @NonNull
    Page<UserResponseDto> searchByEmail(@NonNull String email, @NonNull Pageable pageable);

    @NonNull
    DeleteUserResponseDto deleteCurrentUser(@NonNull User user,
            @NonNull LoginRequestDto requestDto, @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response);

    @NonNull
    UserResponseDto lockUserById(@NonNull Long id);
}
