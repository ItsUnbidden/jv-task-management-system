package com.unbidden.jvtaskmanagementsystem.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;

import com.unbidden.jvtaskmanagementsystem.dto.task.CreateTaskRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.TaskResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.UpdateTaskRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.UpdateTaskStatusRequestDto;
import com.unbidden.jvtaskmanagementsystem.model.User;

public interface TaskService {

    Page<TaskResponseDto> getTasksForUser(@NonNull User user, Pageable pageable);

    Page<TaskResponseDto> getProjectTasks(@NonNull User user, @NonNull Long projectId,
            Pageable pageable);

    Page<TaskResponseDto> getTasksForUserInProjectById(@NonNull User user,
            @NonNull Long projectId, @NonNull Long userId, Pageable pageable);

    TaskResponseDto getTaskById(@NonNull User user, @NonNull Long taskId);

    TaskResponseDto createTaskInProject(@NonNull User user,
            @NonNull Long projectId,
            @NonNull CreateTaskRequestDto requestDto);

    TaskResponseDto updateTask(@NonNull User user, @NonNull Long taskId,
            @NonNull UpdateTaskRequestDto requestDto);

    void deleteTask(@NonNull User user, @NonNull Long taskId);

    TaskResponseDto changeStatus(@NonNull User user, @NonNull Long taskId,
            @NonNull UpdateTaskStatusRequestDto requestDto);

    Page<TaskResponseDto> getTasksByLabelId(@NonNull User user, @NonNull Long labelId,
            Pageable pageable);
}
