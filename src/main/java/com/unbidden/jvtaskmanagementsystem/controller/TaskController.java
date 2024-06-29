package com.unbidden.jvtaskmanagementsystem.controller;

import com.unbidden.jvtaskmanagementsystem.dto.task.CreateTaskRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.TaskResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.UpdateTaskRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.UpdateTaskStatusRequestDto;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.service.TaskService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tasks")
@SuppressWarnings("null")
@Tag(name = "Task related methods")
public class TaskController {
    private final TaskService taskService;

    @GetMapping("/me")
    @Operation(
            summary = "Get current user's tasks",
            responses = {
                @ApiResponse(
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = TaskResponseDto.class)),
                    responseCode = "200",
                    description = "List of tasks"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "401",
                    description = "Unauthorized")
            }
    )
    public List<TaskResponseDto> getTasksForUser(Authentication authentication,
            @Parameter(
                description = "Pagination and sorting"
            )
            Pageable pageable) {
        return taskService.getTasksForUser((User)authentication.getPrincipal(), pageable);
    }

    @GetMapping("/projects/{projectId}/users/{userId}")
    @Operation(
            summary = "Get current user's tasks in project by id",
            responses = {
                @ApiResponse(
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = TaskResponseDto.class)),
                    responseCode = "200",
                    description = "List of tasks"),
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
                    description = "Forbidden. Possible only if project is private")
            }
    )
    public List<TaskResponseDto> getTasksForUserInProjectById(Authentication authentication,
            @Parameter(
                description = "Project id"
            )
            @NonNull @PathVariable Long projectId,
            @Parameter(
                description = "User id"
            )
            @NonNull @PathVariable Long userId,
            @Parameter(
                description = "Pagination and sorting"
            )
            Pageable pageable) {
        return taskService.getTasksForUserInProjectById((User)authentication.getPrincipal(),
                projectId, userId, pageable);
    }

    @GetMapping("projects/{projectId}")
    @Operation(
            summary = "Get tasks in project by id",
            responses = {
                @ApiResponse(
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = TaskResponseDto.class)),
                    responseCode = "200",
                    description = "List of tasks"),
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
                    description = "Forbidden. Possible only if project is private")
            }
    )
    public List<TaskResponseDto> getProjectTasks(Authentication authentication,
            @Parameter(
                description = "Project id"
            )
            @NonNull @PathVariable Long projectId,
            @Parameter(
                description = "Pagination and sorting"
            )
            Pageable pageable) {
        return taskService.getProjectTasks((User)authentication.getPrincipal(),
                projectId, pageable);
    }

    @GetMapping("/{taskId}")
    @Operation(
            summary = "Get task by id",
            responses = {
                @ApiResponse(
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = TaskResponseDto.class)),
                    responseCode = "200",
                    description = "The task"),
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
                    description = "Forbidden. Possible only if project is private")
            }
    )
    public TaskResponseDto getTaskById(Authentication authentication,
            @Parameter(
                description = "Task id"
            )
            @NonNull @PathVariable Long taskId) {
        return taskService.getTaskById((User)authentication.getPrincipal(),
                taskId);
    }

    @GetMapping("/labels/{labelId}")
    @Operation(
            summary = "Get tasks by label id",
            responses = {
                @ApiResponse(
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = TaskResponseDto.class)),
                    responseCode = "200",
                    description = "List of tasks"),
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
                    description = "Forbidden. Possible only if project is private")
            }
    )
    public List<TaskResponseDto> getTasksByLabelId(Authentication authentication,
            @Parameter(
                description = "Label id"
            )
            @NonNull @PathVariable Long labelId,
            @Parameter(
                description = "Pagination and sorting"
            )
            Pageable pageable) {
        return taskService.getTasksByLabelId((User)authentication.getPrincipal(),
                labelId, pageable);
    }
    
    @PostMapping()
    @Operation(
            summary = "Create new task in project",
            responses = {
                @ApiResponse(
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = TaskResponseDto.class)),
                    responseCode = "200",
                    description = "New task"),
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
    public TaskResponseDto createTaskInProject(Authentication authentication,
            @Parameter(
                description = "Create task request dto"
            )
            @NonNull @Valid @RequestBody CreateTaskRequestDto requestDto) {
        return taskService.createTaskInProject((User)authentication.getPrincipal(), 
                requestDto.getProjectId(), requestDto);
    }
    
    @PutMapping("/{taskId}")
    @Operation(
            summary = "Update task by id",
            responses = {
                @ApiResponse(
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = TaskResponseDto.class)),
                    responseCode = "200",
                    description = "Updated task"),
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
    public TaskResponseDto updateTask(Authentication authentication,
            @Parameter(
                description = "Task id"
            )
            @NonNull @PathVariable Long taskId,
            @Parameter(
                description = "Update task request dto"
            )
            @NonNull @Valid @RequestBody UpdateTaskRequestDto requestDto) {
        return taskService.updateTask((User)authentication.getPrincipal(), taskId, requestDto);
    }

    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete task by id",
            responses = {               
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "204"), 
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
                    description = "Forbidden")
            }
    )
    public void deleteTask(Authentication authentication,
            @Parameter(
                description = "Task id"
            )
            @NonNull @PathVariable Long taskId) {
        taskService.deleteTask((User)authentication.getPrincipal(), taskId);
    }

    @PatchMapping("/{taskId}/status")
    @Operation(
            summary = "Change task status",
            description = "Only available for the task assignee or app manager",
            responses = {
                @ApiResponse(
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = TaskResponseDto.class)),
                    responseCode = "200",
                    description = "Updated task"),
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
    public TaskResponseDto changeStatus(Authentication authentication,
            @Parameter(
                description = "Task id"
            )
            @NonNull @PathVariable Long taskId,
            @Parameter(
                description = "Change status request dto"
            )
            @NonNull @Valid @RequestBody UpdateTaskStatusRequestDto requestDto) {
        return taskService.changeStatus((User)authentication.getPrincipal(), taskId, requestDto);
    }
}
