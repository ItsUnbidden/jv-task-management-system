package com.unbidden.jvtaskmanagementsystem.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.unbidden.jvtaskmanagementsystem.config.MapperConfig;
import com.unbidden.jvtaskmanagementsystem.dto.auth.RegistrationRequest;
import com.unbidden.jvtaskmanagementsystem.dto.user.UserResponseDto;
import com.unbidden.jvtaskmanagementsystem.model.Role;
import com.unbidden.jvtaskmanagementsystem.model.Role.RoleType;
import com.unbidden.jvtaskmanagementsystem.model.User;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    @Mapping(target = "roles", source = "roles")
    UserResponseDto toDto(User user);

    @Mapping(target = "locked", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    User toModel(RegistrationRequest registrationRequest);

    default RoleType rolesToRoleTypes(Role role) {
        return role != null ? role.getRoleType() : null;
    }
}
