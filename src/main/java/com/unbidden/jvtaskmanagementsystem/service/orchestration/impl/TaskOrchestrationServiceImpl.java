package com.unbidden.jvtaskmanagementsystem.service.orchestration.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.unbidden.jvtaskmanagementsystem.dto.task.CreateTaskRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.DeleteTaskResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.TaskResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.UpdateTaskRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.UpdateTaskStatusRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.specification.TaskFilterDto;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.CreatedTaskFolderResult;
import com.unbidden.jvtaskmanagementsystem.mapper.TaskMapper;
import com.unbidden.jvtaskmanagementsystem.model.Label;
import com.unbidden.jvtaskmanagementsystem.model.Project;
import com.unbidden.jvtaskmanagementsystem.model.ProjectRole.ProjectRoleType;
import com.unbidden.jvtaskmanagementsystem.model.Task;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.security.project.ProjectSecurity;
import com.unbidden.jvtaskmanagementsystem.service.DropboxService;
import com.unbidden.jvtaskmanagementsystem.service.GoogleCalendarService;
import com.unbidden.jvtaskmanagementsystem.service.TaskService;
import com.unbidden.jvtaskmanagementsystem.service.orchestration.TaskOrchestrationService;
import com.unbidden.jvtaskmanagementsystem.util.EntityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskOrchestrationServiceImpl implements TaskOrchestrationService {
    private final TaskService taskService;

    private final TaskMapper taskMapper;

    private final DropboxService dropboxService;

    private final GoogleCalendarService calendarService;

    private final EntityUtil entityUtil;

    @NonNull
    @Override
    public Page<TaskResponseDto> getTasksForUserAndSearchByTaskName(@NonNull User user,
            @NonNull String name, @NonNull Pageable pageable) {
        return taskService.getTasksForUserAndSearchByTaskName(user, name, pageable)
                .map(taskMapper::toDto);
    }

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, bypassIfPublic = true)
    public Page<TaskResponseDto> getProjectTasks(@NonNull User user, @NonNull Long projectId,
            @NonNull Pageable pageable) {
        return taskService.getProjectTasks(user, projectId, pageable).map(taskMapper::toDto);
    }

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, bypassIfPublic = true)
    public Page<TaskResponseDto> getTasksForUserInProjectById(@NonNull User user,
            @NonNull Long projectId, @NonNull Long userId, @NonNull Pageable pageable) {
        return taskService.getTasksForUserInProjectById(user, projectId, userId, pageable)
                .map(taskMapper::toDto);
    }

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, bypassIfPublic = true,
            entityIdClass = Task.class)
    public TaskResponseDto getTaskById(@NonNull User user, @NonNull Long taskId) {
        return taskMapper.toDto(taskService.getTaskById(user, taskId));
    }

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, bypassIfPublic = true,
            entityIdClass = Label.class)
    public Page<TaskResponseDto> getTasksByLabelId(@NonNull User user, @NonNull Long labelId,
            @NonNull Pageable pageable) {
        return taskService.getTasksByLabelId(user, labelId, pageable).map(taskMapper::toDto);
    }

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, bypassIfPublic = true)
    public Page<TaskResponseDto> getTasksInProjectBySpecification(@NonNull User user,
            @NonNull Long projectId, @NonNull TaskFilterDto filterDto,
            @NonNull Pageable pageable) {
        return taskService.getTasksInProjectBySpecification(user, projectId, filterDto, pageable)
                .map(taskMapper::toDto);
    }

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.ADMIN)
    public TaskResponseDto createTaskInProject(@NonNull User user, @NonNull Long projectId,
            @NonNull CreateTaskRequestDto requestDto) {
        final Project project = entityUtil.getProjectById(projectId);
        final User authorizedUser = (entityUtil.isManager(user))
                ? entityUtil.getProjectOwner(project) : user;
        final Task task = taskMapper.toModel(requestDto);
        
        task.setAssignee((requestDto.getAssigneeId() == null) ? authorizedUser
                : entityUtil.getUserById(requestDto.getAssigneeId()));
        task.setProject(project);
        final CreatedTaskFolderResult dropboxResult = dropboxService.createTaskFolder(authorizedUser, task);
        calendarService.createEventForTask(authorizedUser, task);

        return taskMapper.toDto(taskService.createTaskInProject(
                user, projectId, task, dropboxResult));
    }

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.ADMIN,
            entityIdClass = Task.class)
    public TaskResponseDto updateTask(@NonNull User user, @NonNull Long taskId,
            @NonNull UpdateTaskRequestDto requestDto) {
        final Task taskFromDb = entityUtil.getTaskById(taskId);
        final User authorizedUser = (entityUtil.isManager(user))
                ? entityUtil.getProjectOwner(taskFromDb.getProject()) : user;
        
        final Task resultTask = taskService.updateTask(user, taskId, requestDto);
        calendarService.changeTaskEventDueDate(authorizedUser, resultTask,
                requestDto.getDueDate());
        return taskMapper.toDto(resultTask);
    }

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.ADMIN, entityIdClass = Task.class)
    public DeleteTaskResponseDto deleteTask(@NonNull User user, @NonNull Long taskId) {
        final Task task = entityUtil.getTaskById(taskId);
        final User authorizedUser = (entityUtil.isManager(user))
                ? entityUtil.getProjectOwner(task.getProject()) : user;

        final DeleteTaskResponseDto response = new DeleteTaskResponseDto(
                task.getName(),
                dropboxService.deleteTaskFolder(authorizedUser, task),
                calendarService.deleteTaskEvent(authorizedUser, task));
        taskService.deleteTask(user, taskId);
        return response;
    }

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, entityIdClass = Task.class)
    public TaskResponseDto changeStatus(@NonNull User user, @NonNull Long taskId,
            @NonNull UpdateTaskStatusRequestDto requestDto) {
        return taskMapper.toDto(taskService.changeStatus(user, taskId, requestDto));
    }
}
