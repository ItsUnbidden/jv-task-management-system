package com.unbidden.jvtaskmanagementsystem.dto.user;

import java.util.Set;

import com.unbidden.jvtaskmanagementsystem.model.Role.RoleType;

import lombok.Data;

@Data
public class UserResponseDto {
    private Long id;

    private String username;

    private String email;

    private String firstName;

    private String lastName;

    private boolean isLocked;

    private Set<RoleType> roles;
}
