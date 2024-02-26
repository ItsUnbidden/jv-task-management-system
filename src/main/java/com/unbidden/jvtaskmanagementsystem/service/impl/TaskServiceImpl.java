package com.unbidden.jvtaskmanagementsystem.service.impl;

import com.unbidden.jvtaskmanagementsystem.dto.task.CreateTaskRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.TaskResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.UpdateTaskRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.UpdateTaskStatusRequestDto;
import com.unbidden.jvtaskmanagementsystem.mapper.TaskMapper;
import com.unbidden.jvtaskmanagementsystem.model.Project;
import com.unbidden.jvtaskmanagementsystem.model.ProjectRole.ProjectRoleType;
import com.unbidden.jvtaskmanagementsystem.model.Task;
import com.unbidden.jvtaskmanagementsystem.model.Task.TaskStatus;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.repository.TaskRepository;
import com.unbidden.jvtaskmanagementsystem.security.project.ProjectSecurity;
import com.unbidden.jvtaskmanagementsystem.service.TaskService;
import com.unbidden.jvtaskmanagementsystem.service.util.EntityUtil;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;

    private final TaskMapper taskMapper;

    private final EntityUtil entityUtil;

    @Override
    public List<TaskResponseDto> getTasksForUser(User user, Pageable pageable) {
        List<Task> tasks = taskRepository.findByAssigneeId(user.getId(), pageable);

        tasks.stream().forEach(t -> updateTaskStatusAccordingToDate(t, true));
        return tasks.stream()
                .map(taskMapper::toDto)
                .toList();
    }

    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, includePrivacyCheck = true)
    public List<TaskResponseDto> getProjectTasks(User user, @NonNull Long projectId,
            Pageable pageable) {
        List<Task> tasks = taskRepository.findByProjectId(projectId, pageable);

        tasks.stream().forEach(t -> updateTaskStatusAccordingToDate(t, true));
        return tasks.stream()
                .map(taskMapper::toDto)
                .toList();
    }

    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, includePrivacyCheck = true)
    public List<TaskResponseDto> getTasksForUserInProjectById(User user, @NonNull Long projectId,
            @NonNull Long userId, Pageable pageable) {
        List<Task> tasks = taskRepository
                .findByAssigneeIdAndByProjectId(userId, projectId, pageable);
        
        tasks.stream().forEach(t -> updateTaskStatusAccordingToDate(t, true));
        return tasks.stream()
                .map(taskMapper::toDto)
                .toList();
    }

    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, includePrivacyCheck = true,
            entityIdParamName = "taskId", entityIdClass = Task.class)
    public TaskResponseDto getTaskById(User user, @NonNull Long taskId) {
        Task task = entityUtil.getTaskById(taskId);

        updateTaskStatusAccordingToDate(task, true);
        return taskMapper.toDto(task);
    }

    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.ADMIN)
    public TaskResponseDto createTaskInProject(User user, 
            @NonNull Long projectId,
            @NonNull CreateTaskRequestDto requestDto) {
        Project project = entityUtil.getProjectById(projectId);

        Task task = taskMapper.toModel(requestDto);
        task.setProject(project);
        task.setAssignee(user);
        task.setStatus(TaskStatus.NOT_STARTED);
        updateTaskStatusAccordingToDate(task, false);
        return taskMapper.toDto(taskRepository.save(task));
    }

    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.ADMIN,
            entityIdParamName = "taskId", entityIdClass = Task.class)
    public TaskResponseDto updateTask(User user, @NonNull Long taskId,
            @NonNull UpdateTaskRequestDto requestDto) {
        final Task taskFromDb = entityUtil.getTaskById(taskId);
        
        if (requestDto.getName() != null) {
            taskFromDb.setName(requestDto.getName());
        }
        if (requestDto.getDescription() != null) {
            taskFromDb.setDescription(requestDto.getDescription());
        }
        if (requestDto.getPriority() != null) {
            taskFromDb.setPriority(requestDto.getPriority());
        }
        if (requestDto.getDueDate() != null) {
            taskFromDb.setDueDate(requestDto.getDueDate());
        }
        if (requestDto.getNewAssigneeId() != null) {
            taskFromDb.setAssignee(entityUtil.getUserById(requestDto.getNewAssigneeId()));
        }
        updateTaskStatusAccordingToDate(taskFromDb, false);
        return taskMapper.toDto(taskRepository.save(taskFromDb));
    }

    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.ADMIN, entityIdClass = Task.class,
            entityIdParamName = "taskId")
    public void deleteTask(User user, @NonNull Long taskId) {
        taskRepository.deleteById(taskId);
    }

    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, entityIdClass = Task.class,
            entityIdParamName = "taskId")
    public TaskResponseDto changeStatus(User user, @NonNull Long taskId,
            @NonNull UpdateTaskStatusRequestDto requestDto) {
        final Task task = entityUtil.getTaskById(taskId);

        if (!entityUtil.isManager(user) && task.getAssignee().getId() != user.getId()) {
            throw new AccessDeniedException("Only user that is assigned to task " + taskId 
                    + " can change it's status.");
        }

        task.setStatus(requestDto.getNewStatus());
        updateTaskStatusAccordingToDate(task, false);

        return taskMapper.toDto(taskRepository.save(task));
    }

    private void updateTaskStatusAccordingToDate(Task task, boolean doSave) {
        final TaskStatus initialStatus = task.getStatus();

        if (task.getStatus().equals(TaskStatus.COMPLETED) 
                || task.getStatus().equals(TaskStatus.OVERDUE)) {
            return;
        }

        if (task.getDueDate() != null 
                && task.getDueDate().isBefore(LocalDate.now())) {
            task.setStatus(TaskStatus.OVERDUE);
        }
        if (task.getDueDate() != null
                && task.getDueDate().isAfter(LocalDate.now())
                && !task.getStatus().equals(TaskStatus.NOT_STARTED)) {
            task.setStatus(TaskStatus.IN_PROGRESS);
        }

        if (doSave && !initialStatus.equals(task.getStatus())) {
            taskRepository.save(task); 
        }
    }
}
