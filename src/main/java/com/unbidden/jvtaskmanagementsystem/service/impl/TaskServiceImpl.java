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
public class TaskServiceImpl implements TaskService {
    private final ProjectRepository projectRepository;

    private final TaskRepository taskRepository;

    private final TaskMapper taskMapper;

    private final EntityUtil entityUtil;

    private final DropboxService dropboxService;

    private final GoogleCalendarService calendarService;

    @NonNull
    @Override
    public List<TaskResponseDto> getTasksForUser(@NonNull User user, Pageable pageable) {
        List<Task> tasks = taskRepository.findByAssigneeId(user.getId(), pageable);

        tasks.stream().forEach(t -> updateTaskStatusAccordingToDate(t, true));
        return tasks.stream()
                .map(taskMapper::toDto)
                .toList();
    }

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, bypassIfPublic = true)
    public List<TaskResponseDto> getProjectTasks(@NonNull User user, @NonNull Long projectId,
            Pageable pageable) {
        List<Task> tasks = taskRepository.findByProjectId(projectId, pageable);

        tasks.stream().forEach(t -> updateTaskStatusAccordingToDate(t, true));
        return tasks.stream()
                .map(taskMapper::toDto)
                .toList();
    }

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, bypassIfPublic = true)
    public List<TaskResponseDto> getTasksForUserInProjectById(@NonNull User user,
            @NonNull Long projectId, @NonNull Long userId, Pageable pageable) {
        List<Task> tasks = taskRepository
                .findByAssigneeIdAndByProjectId(userId, projectId, pageable);
        
        tasks.stream().forEach(t -> updateTaskStatusAccordingToDate(t, true));
        return tasks.stream()
                .map(taskMapper::toDto)
                .toList();
    }

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, bypassIfPublic = true,
            entityIdClass = Task.class)
    public TaskResponseDto getTaskById(@NonNull User user, @NonNull Long taskId) {
        Task task = entityUtil.getTaskById(taskId);

        updateTaskStatusAccordingToDate(task, true);
        return taskMapper.toDto(task);
    }

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, bypassIfPublic = true,
            entityIdClass = Label.class)
    public List<TaskResponseDto> getTasksByLabelId(@NonNull User user, @NonNull Long labelId,
            Pageable pageable) {
        List<Task> tasks = taskRepository.findByLabelId(labelId, pageable);

        tasks.stream().forEach(t -> updateTaskStatusAccordingToDate(t, true));
        return tasks.stream()
                .map(taskMapper::toDto)
                .toList();
    }

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.ADMIN)
    public TaskResponseDto createTaskInProject(@NonNull User user, @NonNull Long projectId,
            @NonNull CreateTaskRequestDto requestDto) {
        final Project project = entityUtil.getProjectById(projectId);
        final User authorizedUser = (entityUtil.isManager(user))
                ? entityUtil.getProjectOwner(project) : user;
        
        Task task = taskMapper.toModel(requestDto);
        project.getTasks().add(task);
        task.setProject(project);
        checkDateIsLigit(task);
        task.setStatus(TaskStatus.NOT_STARTED);
        task.setLabels(new HashSet<>());
        task.setAssignee((requestDto.getAssigneeId() == null) ? authorizedUser
                : entityUtil.getUserById(requestDto.getAssigneeId()));
        task.setAmountOfMessages(0);

        dropboxService.createTaskFolder(authorizedUser, task);
        updateTaskStatusAccordingToDate(task, false);
        projectRepository.save(project);
        taskRepository.save(task);
        calendarService.createEventForTask(authorizedUser, task);
        return taskMapper.toDto(task);
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
        
        taskFromDb.setName(requestDto.getName());
        taskFromDb.setDescription(requestDto.getDescription());
        taskFromDb.setDueDate(requestDto.getDueDate());
        checkDateIsLigit(taskFromDb);
        calendarService.changeTaskEventDueDate(authorizedUser, taskFromDb,
                requestDto.getDueDate());
        taskFromDb.setPriority(requestDto.getPriority());
        if (requestDto.getNewAssigneeId() != null) {
            taskFromDb.setAssignee(entityUtil.getUserById(requestDto.getNewAssigneeId()));
        }
        updateTaskStatusAccordingToDate(taskFromDb, false);
        return taskMapper.toDto(taskRepository.save(taskFromDb));
    }

    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.ADMIN, entityIdClass = Task.class)
    public void deleteTask(@NonNull User user, @NonNull Long taskId) {
        final Task task = entityUtil.getTaskById(taskId);
        final User authorizedUser = (entityUtil.isManager(user))
                ? entityUtil.getProjectOwner(task.getProject()) : user;

        dropboxService.deleteTaskFolder(authorizedUser, task);
        calendarService.deleteTaskEvent(authorizedUser, task);
        taskRepository.delete(task);
    }

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, entityIdClass = Task.class)
    public TaskResponseDto changeStatus(@NonNull User user, @NonNull Long taskId,
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

    private void checkDateIsLigit(Task task) {
        if (task.getDueDate() != null) {
            if (task.getProject().getEndDate() == null) {
                if (!task.getDueDate().isAfter(task.getProject().getStartDate())) {
                    throw new IllegalArgumentException("Task's due date is specified as "
                            + task.getDueDate() + " in project where start date is currently "
                            + task.getProject().getStartDate()
                            + ". Task's due date must be after project's start date.");
                }
            } else {
                if (!(task.getDueDate().isAfter(task.getProject().getStartDate())
                        && (task.getDueDate().isBefore(task.getProject().getEndDate())
                        || task.getDueDate().isEqual(task.getProject().getEndDate())))) {
                    throw new IllegalArgumentException("Task's due date is specified as "
                            + task.getDueDate() + " in project where start and end dates "
                            + "currently are " + task.getProject().getStartDate() + " and "
                            + task.getProject().getEndDate() + " respectively. Task's due date "
                            + "must belong to the interval between project's "
                            + "start and end dates.");
                }
            }
        }
    }
}
