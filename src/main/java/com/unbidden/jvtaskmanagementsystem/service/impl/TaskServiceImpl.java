package com.unbidden.jvtaskmanagementsystem.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.unbidden.jvtaskmanagementsystem.dto.task.CreateTaskRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.TaskResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.UpdateTaskRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.UpdateTaskStatusRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.specification.TaskFilterDto;
import com.unbidden.jvtaskmanagementsystem.mapper.TaskMapper;
import com.unbidden.jvtaskmanagementsystem.model.Label;
import com.unbidden.jvtaskmanagementsystem.model.Project;
import com.unbidden.jvtaskmanagementsystem.model.ProjectRole.ProjectRoleType;
import com.unbidden.jvtaskmanagementsystem.model.Task;
import com.unbidden.jvtaskmanagementsystem.model.Task.TaskPriority;
import com.unbidden.jvtaskmanagementsystem.model.Task.TaskStatus;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.repository.LabelRepository;
import com.unbidden.jvtaskmanagementsystem.repository.ProjectRepository;
import com.unbidden.jvtaskmanagementsystem.repository.TaskRepository;
import com.unbidden.jvtaskmanagementsystem.security.project.ProjectSecurity;
import com.unbidden.jvtaskmanagementsystem.service.DropboxService;
import com.unbidden.jvtaskmanagementsystem.service.GoogleCalendarService;
import com.unbidden.jvtaskmanagementsystem.service.TaskService;
import com.unbidden.jvtaskmanagementsystem.util.EntityUtil;

import jakarta.persistence.criteria.Join;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final ProjectRepository projectRepository;

    private final TaskRepository taskRepository;

    private final LabelRepository labelRepository;

    private final TaskMapper taskMapper;

    private final EntityUtil entityUtil;

    private final DropboxService dropboxService;

    private final GoogleCalendarService calendarService;

    @NonNull
    @Override
    public Page<TaskResponseDto> getTasksForUserAndSearchByTaskName(@NonNull User user, @NonNull String name, Pageable pageable) {
        Page<Task> tasks = taskRepository.findByAssigneeIdAndSearchByTaskName(user.getId(), name, pageable);

        tasks.forEach(t -> {
            updateTaskStatusAccordingToDate(t, true);
            t.setLabels(labelRepository.findByTaskId(t.getId()));
        });
        return tasks.map(taskMapper::toDto);
    }

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, bypassIfPublic = true)
    public Page<TaskResponseDto> getProjectTasks(@NonNull User user, @NonNull Long projectId,
            Pageable pageable) {
        Page<Task> tasks = taskRepository.findByProjectId(projectId, pageable);

        tasks.forEach(t -> updateTaskStatusAccordingToDate(t, true));
        return tasks.map(taskMapper::toDto);
    }

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, bypassIfPublic = true)
    public Page<TaskResponseDto> getTasksForUserInProjectById(@NonNull User user,
            @NonNull Long projectId, @NonNull Long userId, Pageable pageable) {
        Page<Task> tasks = taskRepository
                .findByAssigneeIdAndByProjectId(userId, projectId, pageable);
        
        tasks.forEach(t -> updateTaskStatusAccordingToDate(t, true));
        return tasks.map(taskMapper::toDto);
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
    public Page<TaskResponseDto> getTasksByLabelId(@NonNull User user, @NonNull Long labelId,
            Pageable pageable) {
        Page<Task> tasks = taskRepository.findByLabelId(labelId, pageable);

        tasks.forEach(t -> updateTaskStatusAccordingToDate(t, true));
        return tasks.map(taskMapper::toDto);
    }

    @NonNull 
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, bypassIfPublic = true)
    public Page<TaskResponseDto> getTasksInProjectBySpecification(@NonNull User user, @NonNull Long projectId,
            @NonNull TaskFilterDto filterDto, Pageable pageable) {
        Specification<Task> specification = Specification.unrestricted();

        specification = specification.and(TaskSpecifications.hasStatus(filterDto.getStatus()))
                .and(TaskSpecifications.isInProject(projectId))
                .and(TaskSpecifications.isAssignedTo(filterDto.getAssigneeId()))
                .and(TaskSpecifications.hasPriority(filterDto.getPriority()))
                .and(TaskSpecifications.isAfterDueDate(filterDto.getDueDateFrom()))
                .and(TaskSpecifications.isBeforeDueDate(filterDto.getDueDateTo()))
                .and(TaskSpecifications.hasAnyLabels(filterDto.getLabelIds()));
        final Page<TaskResponseDto> tasks = taskRepository.findAll(specification, pageable).map(taskMapper::toDto);
        return tasks;
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
        checkDateIsLegit(task);
        task.setStatus(TaskStatus.NOT_STARTED);
        task.setLabels(List.of());
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
        checkDateIsLegit(taskFromDb);
        calendarService.changeTaskEventDueDate(authorizedUser, taskFromDb,
                requestDto.getDueDate());
        taskFromDb.setPriority(requestDto.getPriority());
        if (requestDto.getNewAssigneeId() != null) {
            taskFromDb.setAssignee(entityUtil.getUserById(requestDto.getNewAssigneeId()));
        }
        taskFromDb.setLabels(labelRepository.findAllById(requestDto.getLabelIds()));
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

        if (!entityUtil.isManager(user) && !task.getAssignee().getId().equals(user.getId())) {
            throw new AccessDeniedException("Only user that is assigned to task " + taskId 
                    + " can change its status.");
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

    private void checkDateIsLegit(Task task) {
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

    private static class TaskSpecifications {
        public static Specification<Task> isAssignedTo(Long userId) {
            return (root, query, cb) -> (userId == null) ? null : cb.equal(root.get("assignee").get("id"), userId);
        }

        public static Specification<Task> isInProject(Long projectId) {
            return (root, query, cb) -> (projectId == null) ? null : cb.equal(root.get("project").get("id"), projectId);
        }

        public static Specification<Task> hasStatus(TaskStatus status) {
            return (root, query, cb) -> (status == null) ? null : cb.equal(root.get("status"), status);
        }

        public static Specification<Task> hasPriority(TaskPriority priority) {
            return (root, query, cb) -> (priority == null) ? null : cb.equal(root.get("priority"), priority);
        }

        public static Specification<Task> isAfterDueDate(LocalDate dueDate) {
            return (root, query, cb) -> (dueDate == null) ? null : cb.greaterThanOrEqualTo(root.get("dueDate"), dueDate);
        }

        public static Specification<Task> isBeforeDueDate(LocalDate dueDate) {
            return (root, query, cb) -> (dueDate == null) ? null : cb.lessThanOrEqualTo(root.get("dueDate"), dueDate);
        }

        public static Specification<Task> hasAnyLabels(List<Long> labelIds) {
            return (root, query, cb) -> {
                if (labelIds == null) return null;

                query.distinct(true);
                final Join<Task, Label> labelsJoin = root.join("labels");
                return labelsJoin.get("id").in(labelIds);
            };
        }
    }
}
