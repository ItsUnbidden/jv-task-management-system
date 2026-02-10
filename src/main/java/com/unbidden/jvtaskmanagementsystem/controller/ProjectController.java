package com.unbidden.jvtaskmanagementsystem.controller;

import com.unbidden.jvtaskmanagementsystem.dto.project.CreateProjectRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.ProjectResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.UpdateProjectRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.UpdateProjectStatusRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.projectrole.UpdateProjectRoleRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.TaskResponseDto;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
@Tag(name = "Project related methods")
public class ProjectController {
    private final ProjectService projectService;

    @GetMapping("/{id}")
    @Operation(
            summary = "Get project by id",
            responses = {
                @ApiResponse(
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ProjectResponseDto.class)),
                    responseCode = "200",
                    description = "The project"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "400",
                    description = "Invalid id"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "401",
                    description = "Unauthorized"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "403",
                    description = "Forbidden. Possible only if project is private")
            }
    )
    public ProjectResponseDto getProjectById(Authentication authentication,
            @Parameter(
                description = "Project id"
            )
            @NonNull @PathVariable Long id) {
        return projectService.findProjectById((User)authentication.getPrincipal(), id);
    }
    
    @GetMapping("/me")
    @Operation(
            summary = "Get all projects the user is a part of",
            responses = {
                @ApiResponse(
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ProjectResponseDto.class)),
                    responseCode = "200",
                    description = "List of projects"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "401",
                    description = "Unauthorized")
            }
    )
    public List<ProjectResponseDto> getAllProjectsForUser(Authentication authentication, 
            @Parameter(
                description = "Pagination and sorting"
            )
            Pageable pageable) {
        return projectService.findAllProjectsForUser((User)authentication.getPrincipal(), pageable);
    }

    @GetMapping("/search")
    @Operation(
            summary = "Search projects by name",
            description = "For MANAGERs projects are not filtered by publicity",
            responses = {
                @ApiResponse(
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ProjectResponseDto.class)),
                    responseCode = "200",
                    description = "List of projects"),
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
    public List<ProjectResponseDto> searchProjectsByName(Authentication authentication,
            @Parameter(
                description = "Search query"
            )
            @NonNull @RequestParam String name,
            @Parameter(
                description = "Pagination and sorting"
            )
            Pageable pageable) {
        return projectService.searchProjectsByName((User)authentication.getPrincipal(),
                name, pageable);
    }
    
    @PostMapping
    @Operation(
            summary = "Create new project",
            description = "Behavior may depend on whether the user has "
                    + "dropbox and/or google connected",
            responses = {
                @ApiResponse(
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ProjectResponseDto.class)),
                    responseCode = "200",
                    description = "New project"),
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
    public ProjectResponseDto createProject(Authentication authentication,
            @Parameter(
                description = "Create project request dto"
            )
            @NonNull @RequestBody @Valid CreateProjectRequestDto requestDto) {
        return projectService.createProject((User)authentication.getPrincipal(), requestDto);
    }
    
    @PutMapping("/{id}")
    @Operation(
            summary = "Update project details",
            description = "Behavior may depend on whether the user has "
                    + "dropbox and/or google connected",
            responses = {
                @ApiResponse(
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ProjectResponseDto.class)),
                    responseCode = "200",
                    description = "Updated project"),
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
    public ProjectResponseDto updateProject(Authentication authentication,
            @Parameter(
                description = "Project id"
            )
            @NonNull @PathVariable Long id,
            @Parameter(
                description = "Update project request dto"
            )
            @NonNull @RequestBody @Valid UpdateProjectRequestDto requestDto) {
        return projectService.updateProject((User)authentication.getPrincipal(), id, requestDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete project",
            description = "Behavior may depend on whether the user has "
                    + "dropbox and/or google connected",
            responses = {
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "204"),
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
    public void deleteProject(Authentication authentication,
            @Parameter(
                description = "Project id"
            )
            @NonNull @PathVariable Long id) {
        projectService.deleteProject((User)authentication.getPrincipal(), id);
    }

    @PostMapping("/{projectId}/users/{userId}/add")
    @Operation(
            summary = "Add new user to project",
            description = "Behavior may depend on whether the user has "
                    + "dropbox and/or google connected",
            responses = {
                @ApiResponse(
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ProjectResponseDto.class)),
                    responseCode = "200",
                    description = "Updated project"),
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
    public ProjectResponseDto addUserToProject(Authentication authentication,
            @Parameter(
                description = "Project id"
            )
            @NonNull @PathVariable Long projectId,
            @Parameter(
                description = "User id"
            )
            @NonNull @PathVariable Long userId) {
        return projectService.addUserToProject((User)authentication.getPrincipal(),
                projectId, userId);
    }

    @DeleteMapping("/{projectId}/users/{userId}/remove")
    @Operation(
            summary = "Remove user from project",
            description = "Behavior may depend on whether the user has "
                    + "dropbox and/or google connected",
            responses = {
                @ApiResponse(
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ProjectResponseDto.class)),
                    responseCode = "200",
                    description = "Updated project"),
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
    public ProjectResponseDto removeUserFromProject(Authentication authentication,
            @Parameter(
                description = "Project id"
            )
            @NonNull @PathVariable Long projectId,
            @Parameter(
                description = "User id"
            )
            @NonNull @PathVariable Long userId) {
        return projectService.removeUserFromProject((User)authentication.getPrincipal(),
                projectId, userId);
    }

    @DeleteMapping("/{projectId}/quit")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Remove current user from project",
            description = "Behavior may depend on whether the user has "
                    + "dropbox and/or google connected",
            responses = {
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "204"),
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
                    description = "Forbidden. Possible if user is not a part of the project")
            }
    )
    public void quitProject(Authentication authentication,
            @Parameter(
                description = "Project id"
            )
            @NonNull @PathVariable Long projectId) {
        final User user = (User)authentication.getPrincipal();

        projectService.quitProject(user, projectId);
    }

    @PatchMapping("/{projectId}/users/{userId}/roles")
    @Operation(
            summary = "Change user's role in the project",
            description = "Behavior may depend on whether the user has "
                    + "dropbox and/or google connected",
            responses = {
                @ApiResponse(
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ProjectResponseDto.class)),
                    responseCode = "200",
                    description = "Updated project"),
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
    public ProjectResponseDto changeProjectMemberRole(Authentication authentication,
            @Parameter(
                description = "Project id"
            )
            @NonNull @PathVariable Long projectId,
            @Parameter(
                description = "User id"
            )
            @NonNull @PathVariable Long userId,
            @Parameter(
                description = "Update project role request dto"
            )
            @NonNull @RequestBody @Valid UpdateProjectRoleRequestDto requestDto) {
        return projectService.changeProjectMemberRole((User)authentication.getPrincipal(),
                projectId, userId, requestDto);
    }

    @PatchMapping("/{projectId}/status")
    @Operation(
            summary = "Change project status",
            description = "Only available to project CREATOR. Allowed values are "
                    + "<IN_PROGRESS> or <COMPLETED>",
            responses = {
                @ApiResponse(
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = TaskResponseDto.class)),
                    responseCode = "200",
                    description = "Updated project"),
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
    public ProjectResponseDto changeStatus(Authentication authentication,
            @Parameter(
                description = "Project id"
            )
            @NonNull @PathVariable Long projectId,
            @Parameter(
                description = "Change project status request dto"
            )
            @NonNull @Valid @RequestBody UpdateProjectStatusRequestDto requestDto) {
        return projectService.changeStatus((User)authentication.getPrincipal(),
                projectId, requestDto);
    }

    @PatchMapping("/{projectId}/dropbox/connect")
    @Operation(
            summary = "Connect project to dropbox",
            description = "Folows \"rigid\" connection system, meaning that it's going to "
                    + "try to connect all users in the project and if for some it's not"
                    + " possible then the operation will be aborted",
            responses = {
                @ApiResponse(
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ProjectResponseDto.class)),
                    responseCode = "200",
                    description = "Updated project"),
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
    public ProjectResponseDto connectProjectToDropbox(Authentication authentication,
            @Parameter(
                description = "Project id"
            )
            @NonNull @PathVariable Long projectId) {
        return projectService.connectProjectToDropbox((User)authentication.getPrincipal(),
                projectId);
    }

    @PatchMapping("/{projectId}/calendar/connect")
    @Operation(
            summary = "Connect project to google calendar",
            description = "Folows \"flexible\" connection system, meaning that it's going to "
                    + "try to connect all users in the project and if for some it's not"
                    + " possible then only those users will not be connected",
            responses = {
                @ApiResponse(
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ProjectResponseDto.class)),
                    responseCode = "200",
                    description = "Updated project"),
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
    public ProjectResponseDto connectProjectToCalendar(Authentication authentication,
            @Parameter(
                description = "Project id"
            )
            @NonNull @PathVariable Long projectId) {
        return projectService.connectProjectToCalendar((User)authentication.getPrincipal(),
                projectId);
    }

    @PatchMapping("/{projectId}/calendar/join")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Connect current user to the project's calendar",
            description = "Allows users that were not connected automaticaly to join calendar",
            responses = {
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "204"),
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
    public void joinCalendar(Authentication authentication,
            @Parameter(
                description = "Project id"
            )
            @NonNull @PathVariable Long projectId) {
        projectService.joinCalendar((User) authentication.getPrincipal(), projectId);
    }
}
