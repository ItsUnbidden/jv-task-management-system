package com.unbidden.jvtaskmanagementsystem.service;

import com.unbidden.jvtaskmanagementsystem.dto.task.CreateTaskRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.TaskResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.UpdateTaskRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.UpdateTaskStatusRequestDto;
import com.unbidden.jvtaskmanagementsystem.model.User;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;

public interface TaskService {

    List<TaskResponseDto> getTasksForUser(@NonNull User user, Pageable pageable);

    List<TaskResponseDto> getProjectTasks(@NonNull User user, @NonNull Long projectId,
            Pageable pageable);

    List<TaskResponseDto> getTasksForUserInProjectById(@NonNull User user,
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

    List<TaskResponseDto> getTasksByLabelId(@NonNull User user, @NonNull Long labelId,
            Pageable pageable);
}
