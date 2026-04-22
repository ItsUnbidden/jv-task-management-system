package com.unbidden.jvtaskmanagementsystem.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import com.unbidden.jvtaskmanagementsystem.dto.task.UpdateTaskRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.UpdateTaskStatusRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.internal.CreatedTaskFolderResult;
import com.unbidden.jvtaskmanagementsystem.dto.task.specification.TaskFilterDto;
import com.unbidden.jvtaskmanagementsystem.model.Task;
import com.unbidden.jvtaskmanagementsystem.model.User;

public interface TaskService {

    @NonNull    
    Page<Task> getTasksForUserAndSearchByTaskName(@NonNull User user, @NonNull String name, Pageable pageable);

    @NonNull 
    Page<Task> getProjectTasks(@NonNull User user, @NonNull Long projectId,
            Pageable pageable);

    @NonNull     
    Page<Task> getTasksForUserInProjectById(@NonNull User user,
            @NonNull Long projectId, @NonNull Long userId, Pageable pageable);

    @NonNull         
    Task getTaskById(@NonNull User user, @NonNull Long taskId);

    @NonNull 
    Page<Task> getTasksInProjectBySpecification(@NonNull User user, @NonNull Long projectId,
            @NonNull TaskFilterDto filterDto, Pageable pageable);

    @NonNull 
    Task createTaskInProject(@NonNull User user, @NonNull Long projectId,
            @NonNull Task task, @Nullable  CreatedTaskFolderResult dropboxResult);

    @NonNull         
    Task updateTask(@NonNull User user, @NonNull Long taskId,
            @NonNull UpdateTaskRequestDto requestDto);

    void deleteTask(@NonNull User user, @NonNull Long taskId);

    @NonNull 
    Task changeStatus(@NonNull User user, @NonNull Long taskId,
            @NonNull UpdateTaskStatusRequestDto requestDto);

    @NonNull 
    Page<Task> getTasksByLabelId(@NonNull User user, @NonNull Long labelId,
            Pageable pageable);
}
