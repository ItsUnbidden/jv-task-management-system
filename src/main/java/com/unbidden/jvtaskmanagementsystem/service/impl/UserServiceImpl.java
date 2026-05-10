package com.unbidden.jvtaskmanagementsystem.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unbidden.jvtaskmanagementsystem.dto.auth.LoginRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.auth.RegistrationRequest;
import com.unbidden.jvtaskmanagementsystem.dto.project.DeleteProjectResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.RemoveUserFromProjectResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.user.DeleteUserResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.user.UserResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.user.UserUpdateDetailsRequestDto;
import com.unbidden.jvtaskmanagementsystem.exception.ErrorType;
import com.unbidden.jvtaskmanagementsystem.exception.StateCollisionException;
import com.unbidden.jvtaskmanagementsystem.mapper.UserMapper;
import com.unbidden.jvtaskmanagementsystem.model.Project;
import com.unbidden.jvtaskmanagementsystem.model.ProjectRole;
import com.unbidden.jvtaskmanagementsystem.model.ProjectRole.ProjectRoleType;
import com.unbidden.jvtaskmanagementsystem.model.Role;
import com.unbidden.jvtaskmanagementsystem.model.Role.RoleType;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.repository.ProjectRoleRepository;
import com.unbidden.jvtaskmanagementsystem.repository.RoleRepository;
import com.unbidden.jvtaskmanagementsystem.repository.UserRepository;
import com.unbidden.jvtaskmanagementsystem.security.AuthenticationService;
import com.unbidden.jvtaskmanagementsystem.service.UserService;
import com.unbidden.jvtaskmanagementsystem.service.orchestration.ProjectOrchestrationService;
import com.unbidden.jvtaskmanagementsystem.util.EntityUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final AuthenticationService authenticationService;

    private final ProjectOrchestrationService projectService;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final ProjectRoleRepository projectRoleRepository;
    
    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    private final EntityUtil entityUtil;

    private Role ownerRole;

    @NonNull
    @Override
    @Transactional
    public UserResponseDto register(@NonNull RegistrationRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new StateCollisionException(
                    "Cannot register user because user with this email is already registred.",
                    ErrorType.REGISTRATION_EMAIL_TAKEN);
        } else if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new StateCollisionException(
                "Cannot register user because this username has already been used.",
                ErrorType.REGISTRATION_USERNAME_TAKEN);
        }
        Role role = roleRepository.findByRoleType(RoleType.USER).get();
        User user = userMapper.toModel(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Set.of(role));
        userRepository.save(user);
        return userMapper.toDto(user);
    }

    @NonNull
    @Override
    public UserResponseDto findCurrentUser(@NonNull User user) {
        return userMapper.toDto(user);
    }

    @NonNull
    @Override
    @Transactional
    public UserResponseDto updateRoles(@NonNull Long id, @NonNull Set<Role> roles) {
        User user = entityUtil.getUserById(id);
        checkUserIsNotOwner(user, "Owner's roles are not allowed to be changed.");
        if (roles.contains(roleRepository.findByRoleType(RoleType.OWNER).get())) {
            throw new UnsupportedOperationException("OWNER role cannot be assigned.");
        }
        user.setRoles(roles);
        return userMapper.toDto(userRepository.save(user));
    }

    @NonNull
    @Override
    @Transactional
    public UserResponseDto updateUserDetails(@NonNull User user, 
            @NonNull UserUpdateDetailsRequestDto requestDto) {
        user.setUsername(requestDto.getUsername());
        user.setEmail(requestDto.getEmail());
        if (requestDto.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        }
        return userMapper.toDto(userRepository.save(user));
    }

    @NonNull
    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDto> searchByUsername(@NonNull String username, @NonNull Pageable pageable) {
        final Page<User> users = userRepository.searchByUsername(username, pageable);
        final List<Long> userIds = users.stream().map(u -> u.getId()).toList();

        if (!userIds.isEmpty()) userRepository.findAllWithRolesByIds(userIds);
        return users.map(userMapper::toDto);
    }

    @NonNull
    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDto> searchByEmail(@NonNull String email, @NonNull Pageable pageable) {
        final Page<User> users = userRepository.searchByEmail(email, pageable);
        final List<Long> userIds = users.stream().map(u -> u.getId()).toList();

        if (!userIds.isEmpty()) userRepository.findAllWithRolesByIds(userIds);
        return users.map(userMapper::toDto);
    }

    @NonNull
    @Override
    @Transactional
    public DeleteUserResponseDto deleteCurrentUser(@NonNull User user,
            @NonNull LoginRequestDto requestDto, @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response) {
        checkUserIsNotOwner(user, "Owner cannot be deleted.");
        if (user.getUsername().equals(requestDto.getUsername()) 
                && passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            final List<ProjectRole> projectRoles = projectRoleRepository.findByUserId(user.getId());
            final List<DeleteProjectResponseDto> deletedProjects = new ArrayList<>();
            final List<RemoveUserFromProjectResponseDto> quittedProjects = new ArrayList<>();

            for (var role : projectRoles) {
                final Project project = role.getProject();

                if (role.getRoleType().equals(ProjectRoleType.CREATOR)) {
                    final DeleteProjectResponseDto projectResponseDto =
                            projectService.deleteProject(user, project.getId());
                    
                    deletedProjects.add(projectResponseDto);
                } else {
                    final RemoveUserFromProjectResponseDto projectResponseDto =
                            projectService.quitProject(user, project.getId());

                    quittedProjects.add(projectResponseDto);
                }
            }
            final DeleteUserResponseDto responseDto = new DeleteUserResponseDto(deletedProjects, quittedProjects);
                     
            authenticationService.logout(request, response);
            userRepository.deleteById(user.getId());
            return responseDto;
        }
        throw new AccessDeniedException("Provided credentials are invalid. "
                + "Please provide correct username and password.");
    }

    @NonNull
    @Override
    @Transactional
    public UserResponseDto lockUserById(@NonNull Long id) {
        User user = entityUtil.getUserById(id);

        checkUserIsNotOwner(user, "Owner cannot be locked.");
        user.setLocked(!user.isLocked());
        return userMapper.toDto(userRepository.save(user));
    }

    private void checkUserIsNotOwner(User user, String errorMsg) {
        if (ownerRole == null) {
            ownerRole = roleRepository.findByRoleType(RoleType.OWNER).get();
        }

        if (user.getRoles().contains(ownerRole)) {
            throw new UnsupportedOperationException(errorMsg);
        }
    }
}
