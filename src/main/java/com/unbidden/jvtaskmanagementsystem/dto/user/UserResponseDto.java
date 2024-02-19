package com.unbidden.jvtaskmanagementsystem.dto.user;

import com.unbidden.jvtaskmanagementsystem.model.Role;
import java.util.Set;
import lombok.Data;

@Data
public class UserResponseDto {
    private Long id;

    private String username;

    private String email;

    private String firstName;

    private String lastName;

    private boolean isLocked;

    private Set<Role> roles;
}
