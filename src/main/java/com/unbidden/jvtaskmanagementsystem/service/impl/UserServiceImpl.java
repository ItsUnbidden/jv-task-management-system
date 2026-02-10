package com.unbidden.jvtaskmanagementsystem.service.impl;

import com.unbidden.jvtaskmanagementsystem.dto.auth.LoginRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.auth.RegistrationRequest;
import com.unbidden.jvtaskmanagementsystem.dto.user.UserResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.user.UserUpdateDetailsRequestDto;
import com.unbidden.jvtaskmanagementsystem.exception.RegistrationException;
import com.unbidden.jvtaskmanagementsystem.mapper.UserMapper;
import com.unbidden.jvtaskmanagementsystem.model.Role;
import com.unbidden.jvtaskmanagementsystem.model.Role.RoleType;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.repository.RoleRepository;
import com.unbidden.jvtaskmanagementsystem.repository.UserRepository;
import com.unbidden.jvtaskmanagementsystem.service.UserService;
import com.unbidden.jvtaskmanagementsystem.util.EntityUtil;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    private final RoleRepository roleRepository;
    
    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    private final EntityUtil entityUtil;

    private Role ownerRole;

    @NonNull
    @Override
    public UserResponseDto register(@NonNull RegistrationRequest request)
            throws RegistrationException {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RegistrationException(
                    "Cannot register user because user with this email is already registred.");
        } else if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RegistrationException(
                "Cannot register user because this username has already been used.");
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
    public UserResponseDto updateRoles(@NonNull Long id, @NonNull Set<Role> roles) {
        User user = entityUtil.getUserById(id);
        checkUserIsNotOwner(user, "Owner's roles are not allowed to be changed.");
        user.setRoles(roles);
        return userMapper.toDto(userRepository.save(user));
    }

    @NonNull
    @Override
    public UserResponseDto updateUserDetails(@NonNull User user, 
            @NonNull UserUpdateDetailsRequestDto requestDto) {
        user.setUsername(requestDto.getUsername());
        user.setFirstName(requestDto.getFirstName());
        user.setLastName(requestDto.getLastName());
        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        return userMapper.toDto(userRepository.save(user));
    }

    @NonNull
    @Override
    public List<UserResponseDto> findAll(@NonNull Pageable pageable) {
        return userRepository.findAll(pageable).stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    public void deleteCurrentUser(@NonNull User user, @NonNull LoginRequestDto requestDto) {
        checkUserIsNotOwner(user, "Owner cannot be deleted.");
        if (user.getUsername().equals(requestDto.getUsername()) 
                && passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            userRepository.deleteById(user.getId());
            return;
        }
        throw new AccessDeniedException("Provided credentials are invalid. "
                + "Please provide correct username and password.");
    }

    @NonNull
    @Override
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
