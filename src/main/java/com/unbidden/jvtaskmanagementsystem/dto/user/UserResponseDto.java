package com.unbidden.jvtaskmanagementsystem.dto.user;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.unbidden.jvtaskmanagementsystem.model.Role.RoleType;

import lombok.Data;

@Data
public class UserResponseDto {
    private Long id;

    private String username;

    private String email;

    @JsonProperty("isLocked")
    private boolean isLocked;

    private Set<RoleType> roles;
}
