package com.unbidden.jvtaskmanagementsystem.service.impl;

import com.unbidden.jvtaskmanagementsystem.dto.task.CreateTaskRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.TaskResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.UpdateTaskRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.UpdateTaskStatusRequestDto;
import com.unbidden.jvtaskmanagementsystem.mapper.TaskMapper;
import com.unbidden.jvtaskmanagementsystem.model.Label;
import com.unbidden.jvtaskmanagementsystem.model.Project;
import com.unbidden.jvtaskmanagementsystem.model.ProjectRole.ProjectRoleType;
import com.unbidden.jvtaskmanagementsystem.model.Task;
import com.unbidden.jvtaskmanagementsystem.model.Task.TaskStatus;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.repository.ProjectRepository;
import com.unbidden.jvtaskmanagementsystem.repository.TaskRepository;
import com.unbidden.jvtaskmanagementsystem.security.project.ProjectSecurity;
import com.unbidden.jvtaskmanagementsystem.service.DropboxService;
import com.unbidden.jvtaskmanagementsystem.service.GoogleCalendarService;
import com.unbidden.jvtaskmanagementsystem.service.TaskService;
import com.unbidden.jvtaskmanagementsystem.util.EntityUtil;
import java.time.LocalDate;
import java.util.HashSet;
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
    private final ProjectRepository projectRepository;

    private final TaskRepository taskRepository;

    private final TaskMapper taskMapper;

    private final EntityUtil entityUtil;

    private final DropboxService dropboxService;

    private final GoogleCalendarService calendarService;

    @Override
    public List<TaskResponseDto> getTasksForUser(User user, Pageable pageable) {
        List<Task> tasks = taskRepository.findByAssigneeId(user.getId(), pageable);

        tasks.stream().forEach(t -> updateTaskStatusAccordingToDate(t, true));
        return tasks.stream()
                .map(taskMapper::toDto)
                .toList();
    }

    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, bypassIfPublic = true)
    public List<TaskResponseDto> getProjectTasks(User user, @NonNull Long projectId,
            Pageable pageable) {
        List<Task> tasks = taskRepository.findByProjectId(projectId, pageable);

        tasks.stream().forEach(t -> updateTaskStatusAccordingToDate(t, true));
        return tasks.stream()
                .map(taskMapper::toDto)
                .toList();
    }

    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, bypassIfPublic = true)
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
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, bypassIfPublic = true,
            entityIdClass = Task.class)
    public TaskResponseDto getTaskById(User user, @NonNull Long taskId) {
        Task task = entityUtil.getTaskById(taskId);

        updateTaskStatusAccordingToDate(task, true);
        return taskMapper.toDto(task);
    }

    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, bypassIfPublic = true,
            entityIdClass = Label.class)
    public List<TaskResponseDto> getTasksByLabelId(User user, @NonNull Long labelId,
            Pageable pageable) {
        List<Task> tasks = taskRepository.findByLabelId(labelId, pageable);

        tasks.stream().forEach(t -> updateTaskStatusAccordingToDate(t, true));
        return tasks.stream()
                .map(taskMapper::toDto)
                .toList();
    }

    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.ADMIN)
    public TaskResponseDto createTaskInProject(User user, @NonNull Long projectId,
            @NonNull CreateTaskRequestDto requestDto) {
        final Project project = entityUtil.getProjectById(projectId);
        
        Task task = taskMapper.toModel(requestDto);
        project.getTasks().add(task);
        task.setProject(project);
        task.setStatus(TaskStatus.NOT_STARTED);
        task.setLabels(new HashSet<>());
        task.setAssignee((requestDto.getAssigneeId() == null) ? user
                : entityUtil.getUserById(requestDto.getAssigneeId()));

        dropboxService.createTaskFolder(user, task);
        updateTaskStatusAccordingToDate(task, false);
        projectRepository.save(project);
        taskRepository.save(task);
        calendarService.createEventForTask(user, task);
        return taskMapper.toDto(task);
    }

    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.ADMIN,
            entityIdClass = Task.class)
    public TaskResponseDto updateTask(User user, @NonNull Long taskId,
            @NonNull UpdateTaskRequestDto requestDto) {
        final Task taskFromDb = entityUtil.getTaskById(taskId);
        
        calendarService.changeTaskEventDueDate(user, taskFromDb, requestDto.getDueDate());
        taskFromDb.setName(requestDto.getName());
        taskFromDb.setDescription(requestDto.getDescription());
        taskFromDb.setDueDate(requestDto.getDueDate());
        taskFromDb.setPriority(requestDto.getPriority());
        if (requestDto.getNewAssigneeId() != null) {
            taskFromDb.setAssignee(entityUtil.getUserById(requestDto.getNewAssigneeId()));
        }
        updateTaskStatusAccordingToDate(taskFromDb, false);
        return taskMapper.toDto(taskRepository.save(taskFromDb));
    }

    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.ADMIN, entityIdClass = Task.class)
    public void deleteTask(User user, @NonNull Long taskId) {
        final Task task = entityUtil.getTaskById(taskId);

        dropboxService.deleteTaskFolder(user, task);
        calendarService.deleteTaskEvent(user, task);
        taskRepository.delete(task);
    }

    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, entityIdClass = Task.class)
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
