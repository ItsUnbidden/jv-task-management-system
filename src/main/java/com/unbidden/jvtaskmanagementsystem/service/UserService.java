package com.unbidden.jvtaskmanagementsystem.service;

import com.unbidden.jvtaskmanagementsystem.dto.auth.LoginRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.auth.RegistrationRequest;
import com.unbidden.jvtaskmanagementsystem.dto.user.UserResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.user.UserUpdateDetailsRequestDto;
import com.unbidden.jvtaskmanagementsystem.exception.RegistrationException;
import com.unbidden.jvtaskmanagementsystem.model.Role;
import com.unbidden.jvtaskmanagementsystem.model.User;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;

public interface UserService {
    UserResponseDto register(@NonNull RegistrationRequest request) throws RegistrationException;

    UserResponseDto findCurrentUser(User user);

    UserResponseDto updateRoles(@NonNull Long id, Set<Role> roles);

    UserResponseDto updateUserDetails(User user,
            @NonNull UserUpdateDetailsRequestDto requestDto);

    List<UserResponseDto> findAll(@NonNull Pageable pageable);

    void deleteCurrentUser(User user, @NonNull LoginRequestDto requestDto);

    UserResponseDto lockUserById(@NonNull Long id);
}
