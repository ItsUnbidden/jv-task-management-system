package com.unbidden.jvtaskmanagementsystem.service;

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;

import com.unbidden.jvtaskmanagementsystem.dto.auth.LoginRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.auth.RegistrationRequest;
import com.unbidden.jvtaskmanagementsystem.dto.user.UserResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.user.UserUpdateDetailsRequestDto;
import com.unbidden.jvtaskmanagementsystem.exception.RegistrationException;
import com.unbidden.jvtaskmanagementsystem.model.Role;
import com.unbidden.jvtaskmanagementsystem.model.User;

public interface UserService {
    UserResponseDto register(@NonNull RegistrationRequest request) throws RegistrationException;

    UserResponseDto findCurrentUser(@NonNull User user);

    UserResponseDto updateRoles(@NonNull Long id, @NonNull Set<Role> roles);

    UserResponseDto updateUserDetails(@NonNull User user,
            @NonNull UserUpdateDetailsRequestDto requestDto);

    Page<UserResponseDto> findAll(@NonNull Pageable pageable);

    void deleteCurrentUser(@NonNull User user, @NonNull LoginRequestDto requestDto);

    UserResponseDto lockUserById(@NonNull Long id);
}
