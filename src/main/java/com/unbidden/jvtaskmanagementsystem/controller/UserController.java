package com.unbidden.jvtaskmanagementsystem.controller;

import com.unbidden.jvtaskmanagementsystem.dto.auth.LoginRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.user.UserResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.user.UserUpdateDetailsRequestDto;
import com.unbidden.jvtaskmanagementsystem.model.Role;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private static final Logger LOGGER = LogManager.getLogger(UserController.class);

    private final UserService userService;

    @GetMapping("/me")
    public UserResponseDto getCurrentUser(Authentication authentication) {
        return userService.findCurrentUser((User)authentication.getPrincipal());
    }

    @PreAuthorize("hasRole('OWNER')")
    @PatchMapping("/{id}/roles")
    public UserResponseDto updateRoles(@NonNull @RequestBody Set<Role> roles,
            @NonNull @PathVariable Long id) {
        return userService.updateRoles(id, roles);
    }

    @PutMapping("/me")
    public UserResponseDto updateUserDetails(Authentication authentication,
            @NonNull @RequestBody @Valid UserUpdateDetailsRequestDto requestDto) {
        return userService.updateUserDetails((User)authentication.getPrincipal(), requestDto);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping()
    public List<UserResponseDto> getAllUsers(@NonNull Pageable pageable) {
        return userService.findAll(pageable);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCurrentUser(Authentication authentication, 
            @NonNull @RequestBody LoginRequestDto requestDto) {
        userService.deleteCurrentUser((User)authentication.getPrincipal(), requestDto);
    }

    @PatchMapping("/{id}/lock")
    @PreAuthorize("hasRole('MANAGER')")
    public UserResponseDto changeLockedStatus(@NonNull @PathVariable Long id) {
        return userService.lockUserById(id);
    }

    //TODO: This is a test endpoint. It needs to be removed
    @GetMapping("/test")
    public String testMethod(HttpServletRequest request) {
        LOGGER.info(Date.from(LocalDate.now().atStartOfDay()
                .atZone(ZoneId.systemDefault()).toInstant()));
        return "This is a test method.";
    }
} 
