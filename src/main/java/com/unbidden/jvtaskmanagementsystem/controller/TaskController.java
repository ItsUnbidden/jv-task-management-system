package com.unbidden.jvtaskmanagementsystem.controller;

import com.unbidden.jvtaskmanagementsystem.dto.task.CreateTaskRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.TaskResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.UpdateTaskRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.UpdateTaskStatusRequestDto;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.service.TaskService;
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
public class TaskController {
    private final TaskService taskService;

    @GetMapping("/me")
    public List<TaskResponseDto> getTasksForUser(Authentication authentication,
            Pageable pageable) {
        return taskService.getTasksForUser((User)authentication.getPrincipal(), pageable);
    }

    @GetMapping("/projects/{projectId}/users/{userId}")
    public List<TaskResponseDto> getTasksForUserInProjectById(Authentication authentication,
            @NonNull @PathVariable Long projectId, @NonNull @PathVariable Long userId,
            Pageable pageable) {
        return taskService.getTasksForUserInProjectById((User)authentication.getPrincipal(),
                projectId, userId, pageable);
    }

    @GetMapping("projects/{projectId}")
    public List<TaskResponseDto> getProjectTasks(Authentication authentication, 
            @NonNull @PathVariable Long projectId, Pageable pageable) {
        return taskService.getProjectTasks((User)authentication.getPrincipal(),
                projectId, pageable);
    }

    @GetMapping("/{taskId}")
    public TaskResponseDto getTaskById(Authentication authentication,
            @NonNull @PathVariable Long taskId) {
        return taskService.getTaskById((User)authentication.getPrincipal(),
                taskId);
    }

    @GetMapping("/labels/{labelId}")
    public List<TaskResponseDto> getTasksByLabelId(Authentication authentication,
            @NonNull @PathVariable Long labelId, Pageable pageable) {
        return taskService.getTasksByLabelId((User)authentication.getPrincipal(),
                labelId, pageable);
    }
    
    @PostMapping()
    public TaskResponseDto createTaskInProject(Authentication authentication,
            @NonNull @Valid @RequestBody CreateTaskRequestDto requestDto) {
        return taskService.createTaskInProject((User)authentication.getPrincipal(), 
                requestDto.getProjectId(), requestDto);
    }
    
    @PutMapping("/{taskId}")
    public TaskResponseDto updateTask(Authentication authentication, 
            @NonNull @PathVariable Long taskId,
            @NonNull @Valid @RequestBody UpdateTaskRequestDto requestDto) {
        return taskService.updateTask((User)authentication.getPrincipal(), taskId, requestDto);
    }

    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(Authentication authentication, @NonNull @PathVariable Long taskId) {
        taskService.deleteTask((User)authentication.getPrincipal(), taskId);
    }

    @PatchMapping("/{taskId}/status") 
    public TaskResponseDto changeStatus(Authentication authentication,
            @NonNull @PathVariable Long taskId,
            @NonNull @Valid @RequestBody UpdateTaskStatusRequestDto requestDto) {
        return taskService.changeStatus((User)authentication.getPrincipal(), taskId, requestDto);
    }
}
