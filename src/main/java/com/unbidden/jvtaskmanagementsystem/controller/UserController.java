package com.unbidden.jvtaskmanagementsystem.controller;

import com.unbidden.jvtaskmanagementsystem.dto.auth.LoginRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.user.UserResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.user.UserUpdateDetailsRequestDto;
import com.unbidden.jvtaskmanagementsystem.model.Role;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
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
@Tag(name = "User related methods")
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    @Operation(
            summary = "Get current user details",
            responses = {
                @ApiResponse(
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = UserResponseDto.class)),
                    responseCode = "200",
                    description = "Current user"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "400",
                    description = "Invalid input"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "401",
                    description = "Unauthorized")
            }
    )
    public UserResponseDto getCurrentUser(Authentication authentication) {
        return userService.findCurrentUser((User)authentication.getPrincipal());
    }

    @PreAuthorize("hasRole('OWNER')")
    @PatchMapping("/{id}/roles")
    @Operation(
            summary = "Update roles for a specific user",
            description = "Requires MANAGER role to access",
            responses = {
                @ApiResponse(
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = UserResponseDto.class)),
                    responseCode = "200",
                    description = "User with updated roles"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "400",
                    description = "Invalid input"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "401",
                    description = "Unauthorized"), 
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "403",
                    description = "Forbidden")  
            }
    )
    public UserResponseDto updateRoles(
            @Parameter(
                description = "New roles"
            )
            @NonNull @RequestBody Set<Role> roles,
            @Parameter(
                description = "User id"
            )
            @NonNull @PathVariable Long id) {
        return userService.updateRoles(id, roles);
    }

    @PutMapping("/me")
    @Operation(
            summary = "Update user details for current user",
            responses = {
                @ApiResponse(
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = UserResponseDto.class)),
                    responseCode = "200",
                    description = "User with updated user details"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "400",
                    description = "Invalid input"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "401",
                    description = "Unauthorized")
            }
    )
    public UserResponseDto updateUserDetails(Authentication authentication,
            @Parameter(
                description = "User update request dto"
            )
            @NonNull @RequestBody @Valid UserUpdateDetailsRequestDto requestDto) {
        return userService.updateUserDetails((User)authentication.getPrincipal(), requestDto);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping()
    @Operation(
            summary = "Find all users",
            description = "Requires MANAGER role to access",
            responses = {
                @ApiResponse(
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = UserResponseDto.class)),
                    responseCode = "200",
                    description = "List of all registred users"), 
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "401",
                    description = "Unauthorized"), 
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "403",
                    description = "Forbidden")  
            }
    )
    public List<UserResponseDto> getAllUsers(
            @Parameter(
                description = "Pagination and sorting"
            )
            @NonNull Pageable pageable) {
        return userService.findAll(pageable);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete current user",
            description = "Requires confirmation through user credentials",
            responses = {               
                @ApiResponse(
                    responseCode = "204"), 
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "400",
                    description = "Invalid credentials"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "401",
                    description = "Unauthorized")
            }
    )
    public void deleteCurrentUser(Authentication authentication,
            @Parameter(
                description = "Login request dto for confirmation"
            )
            @NonNull @RequestBody LoginRequestDto requestDto) {
        userService.deleteCurrentUser((User)authentication.getPrincipal(), requestDto);
    }

    @PatchMapping("/{id}/lock")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Update \"locked\" status for user",
            description = "Requires MANAGER role to access",
            responses = {
                @ApiResponse(
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = UserResponseDto.class)),
                    responseCode = "200",
                    description = "Updated user"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "400",
                    description = "Invalid input"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "401",
                    description = "Unauthorized"), 
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "403",
                    description = "Forbidden")
            }
    )
    public UserResponseDto changeLockedStatus(
            @Parameter(
                description = "User id"
            )
            @NonNull @PathVariable Long id) {
        return userService.lockUserById(id);
    }
} 
