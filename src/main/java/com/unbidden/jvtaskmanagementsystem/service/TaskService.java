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

    List<TaskResponseDto> getTasksForUser(User user, Pageable pageable);

    List<TaskResponseDto> getProjectTasks(User user, @NonNull Long projectId, Pageable pageable);

    List<TaskResponseDto> getTasksForUserInProjectById(User user, @NonNull Long projectId,
            @NonNull Long userId, Pageable pageable);

    TaskResponseDto getTaskById(User user, @NonNull Long taskId);

    TaskResponseDto createTaskInProject(User user,
            @NonNull Long projectId,
            @NonNull CreateTaskRequestDto requestDto);

    TaskResponseDto updateTask(User user, @NonNull Long taskId,
            @NonNull UpdateTaskRequestDto requestDto);

    void deleteTask(User user, @NonNull Long taskId);

    TaskResponseDto changeStatus(User user, @NonNull Long taskId,
            @NonNull UpdateTaskStatusRequestDto requestDto);

    List<TaskResponseDto> getTasksByLabelId(User user, @NonNull Long labelId, Pageable pageable);
}
