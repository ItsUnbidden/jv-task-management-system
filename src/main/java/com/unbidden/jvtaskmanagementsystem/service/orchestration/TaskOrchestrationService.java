package com.unbidden.jvtaskmanagementsystem.service.orchestration;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;

import com.unbidden.jvtaskmanagementsystem.dto.task.CreateTaskRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.DeleteTaskResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.TaskResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.UpdateTaskRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.UpdateTaskStatusRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.specification.TaskFilterDto;
import com.unbidden.jvtaskmanagementsystem.model.User;

public interface TaskOrchestrationService {
    @NonNull    
    Page<TaskResponseDto> getTasksForUserAndSearchByTaskName(@NonNull User user, @NonNull String name, @NonNull Pageable pageable);

    @NonNull 
    Page<TaskResponseDto> getProjectTasks(@NonNull User user, @NonNull Long projectId,
            @NonNull Pageable pageable);

    @NonNull     
    Page<TaskResponseDto> getTasksForUserInProjectById(@NonNull User user,
            @NonNull Long projectId, @NonNull Long userId, @NonNull Pageable pageable);

    @NonNull         
    TaskResponseDto getTaskById(@NonNull User user, @NonNull Long taskId);

    @NonNull 
    Page<TaskResponseDto> getTasksInProjectBySpecification(@NonNull User user, @NonNull Long projectId,
            @NonNull TaskFilterDto filterDto, @NonNull Pageable pageable);

    @NonNull 
    TaskResponseDto createTaskInProject(@NonNull User user,
            @NonNull Long projectId,
            @NonNull CreateTaskRequestDto requestDto);

    @NonNull         
    TaskResponseDto updateTask(@NonNull User user, @NonNull Long taskId,
            @NonNull UpdateTaskRequestDto requestDto);

    @NonNull    
    DeleteTaskResponseDto deleteTask(@NonNull User user, @NonNull Long taskId);

    @NonNull 
    TaskResponseDto changeStatus(@NonNull User user, @NonNull Long taskId,
            @NonNull UpdateTaskStatusRequestDto requestDto);

    @NonNull 
    Page<TaskResponseDto> getTasksByLabelId(@NonNull User user, @NonNull Long labelId,
            @NonNull Pageable pageable);
}
