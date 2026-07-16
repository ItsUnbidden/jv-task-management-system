package com.unbidden.jvtaskmanagementsystem.service.impl;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.unbidden.jvtaskmanagementsystem.dto.internal.SubtasksChanged;
import com.unbidden.jvtaskmanagementsystem.dto.internal.TaskProgressUpdated;
import com.unbidden.jvtaskmanagementsystem.dto.task.UpdateTaskRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.UpdateTaskStatusRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.specification.TaskFilterDto;
import com.unbidden.jvtaskmanagementsystem.exception.EntityNotFoundException;
import com.unbidden.jvtaskmanagementsystem.exception.ErrorType;
import com.unbidden.jvtaskmanagementsystem.exception.InconsistentDataException;
import com.unbidden.jvtaskmanagementsystem.exception.StateCollisionException;
import com.unbidden.jvtaskmanagementsystem.model.Label;
import com.unbidden.jvtaskmanagementsystem.model.Project;
import com.unbidden.jvtaskmanagementsystem.model.Task;
import com.unbidden.jvtaskmanagementsystem.model.Task.TaskPriority;
import com.unbidden.jvtaskmanagementsystem.model.Task.TaskStatus;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.repository.LabelRepository;
import com.unbidden.jvtaskmanagementsystem.repository.ProjectRepository;
import com.unbidden.jvtaskmanagementsystem.repository.SubtaskRepository;
import com.unbidden.jvtaskmanagementsystem.repository.TaskRepository;
import com.unbidden.jvtaskmanagementsystem.service.TaskService;
import com.unbidden.jvtaskmanagementsystem.util.EntityUtil;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final ApplicationEventPublisher eventPublisher;

    private final ProjectRepository projectRepository;

    private final TaskRepository taskRepository;

    private final SubtaskRepository subtaskRepository;

    private final LabelRepository labelRepository;

    private final EntityUtil entityUtil;

    @NonNull
    @Override
    @Transactional
    public Page<Task> getTasksForUserAndSearchByTaskName(@NonNull User user, @NonNull String name,
            @NonNull Pageable pageable) {
        Page<Task> tasks = taskRepository.findByAssigneeIdAndSearchByTaskName(user.getId(), name, pageable);

        tasks.forEach(t -> {
            updateTaskStatusAccordingToDate(t);
            t.setLabels(labelRepository.findByTaskId(t.getId(), Pageable.unpaged()).getContent()); // TODO: N + 1. Consider removing the labels field from the task entity entirely, since it's not really used that much.
        });
        return tasks;
    }

    @NonNull
    @Override
    @Transactional
    public Page<Task> getProjectTasks(@NonNull User user, @NonNull Long projectId,
            @NonNull Pageable pageable) {
        Page<Task> tasks = taskRepository.findByProjectId(projectId, pageable);

        tasks.forEach(t -> updateTaskStatusAccordingToDate(t));
        return tasks;
    }

    @NonNull
    @Override
    @Transactional
    public Page<Task> getTasksForUserInProjectById(@NonNull User user,
            @NonNull Long projectId, @NonNull Long userId, @NonNull Pageable pageable) {
        Page<Task> tasks = taskRepository
                .findByAssigneeIdAndByProjectId(userId, projectId, pageable);
        
        tasks.forEach(t -> updateTaskStatusAccordingToDate(t));
        return tasks;
    }

    @NonNull
    @Override
    @Transactional
    public Task getTaskById(@NonNull User user, @NonNull Long taskId) {
        Task task = entityUtil.getTaskById(taskId);

        updateTaskStatusAccordingToDate(task);
        return task;
    }

    @NonNull
    @Override
    @Transactional
    public Page<Task> getTasksByLabelId(@NonNull User user, @NonNull Long labelId,
            @NonNull Pageable pageable) {
        Page<Task> tasks = taskRepository.findByLabelId(labelId, pageable);

        tasks.forEach(t -> updateTaskStatusAccordingToDate(t));
        return tasks;
    }

    @NonNull 
    @Override
    @Transactional
    public Page<Task> getTasksInProjectBySpecification(@NonNull User user, @NonNull Long projectId,
            @NonNull TaskFilterDto filterDto, @NonNull Pageable pageable) {
        Specification<Task> specification = Specification.unrestricted();

        specification = specification.and(TaskSpecifications.hasStatus(filterDto.getStatus()))
                .and(TaskSpecifications.isInProject(projectId))
                .and(TaskSpecifications.isAssignedTo(filterDto.getAssigneeId()))
                .and(TaskSpecifications.hasPriority(filterDto.getPriority()))
                .and(TaskSpecifications.isAfterDueDate(filterDto.getDueDateFrom()))
                .and(TaskSpecifications.isBeforeDueDate(filterDto.getDueDateTo()))
                .and(filterDto.isAnyLabels()
                        ? TaskSpecifications.hasAnyLabels(filterDto.getLabelIds())
                        : TaskSpecifications.hasAllLabels(filterDto.getLabelIds()));        
        final Page<Task> tasks = taskRepository.findAll(specification, pageable);
        tasks.forEach(t -> updateTaskStatusAccordingToDate(t));
        return tasks;
    }

    @NonNull
    @Override
    @Transactional
    public Task createTaskInProject(@NonNull User user, @NonNull Long projectId,
            @NonNull Task task) {
        final Project project = entityUtil.getProjectById(projectId);

        project.getTasks().add(task);
        task.setProject(project);
        task.setStatus(TaskStatus.NOT_STARTED);
        task.setAmountOfMessages(0);
        task.setProgress(0);
        checkDateIsLegit(task);
        updateTaskStatusAccordingToDate(task);
            
        eventPublisher.publishEvent(new TaskProgressUpdated(project.getId()));

        projectRepository.save(project);
        return taskRepository.save(task);
    }

    @NonNull
    @Override
    @Transactional
    public Task updateTask(@NonNull User user, @NonNull Long taskId,
            @NonNull UpdateTaskRequestDto requestDto) {
        final Task taskFromDb = entityUtil.getTaskById(taskId);

        if (!requestDto.getVersion().equals(taskFromDb.getVersion())) {
            throw new StateCollisionException("Can't update task " + taskFromDb.getName()
                    + ", because the version does not match.", ErrorType.TASK_OPTIMISTIC_LOCK);
        }
        
        taskFromDb.setName(requestDto.getName());
        taskFromDb.setDescription(requestDto.getDescription());
        taskFromDb.setDueDate(requestDto.getDueDate());
        checkDateIsLegit(taskFromDb);
        taskFromDb.setPriority(requestDto.getPriority());

        if (requestDto.getNewAssigneeId() != null) taskFromDb.setAssignee(entityUtil.getUserById(requestDto.getNewAssigneeId()));
        if (requestDto.getLabelIds() != null) taskFromDb.setLabels(labelRepository.findAllById(requestDto.getLabelIds()));
        
        updateTaskStatusAccordingToDate(taskFromDb);
        return taskFromDb;
    }

    @Override
    @Transactional
    public void deleteTask(@NonNull User user, @NonNull Long taskId) {
        final Task task = entityUtil.getTaskById(taskId);

        eventPublisher.publishEvent(new TaskProgressUpdated(task.getProject().getId()));
        
        taskRepository.delete(task);
    }

    @NonNull
    @Override
    @Transactional
    public Task changeStatus(@NonNull User user, @NonNull Long taskId,
            @NonNull UpdateTaskStatusRequestDto requestDto) {
        final Task task = entityUtil.getTaskById(taskId);

        if (!entityUtil.isManager(user) && !task.getAssignee().getId().equals(user.getId())) {
            throw new AccessDeniedException("Only user that is assigned to task " + taskId 
                    + " can change its status.");
        }
        
        task.setStatus(requestDto.getNewStatus());
        
        updateTaskProgress(task);
        
        updateTaskStatusAccordingToDate(task);

        return task;
    }

    @Override
    @Transactional
    public void setDropboxFolderId(@NonNull Long taskId, @NonNull String taskFolderId) {
        final Task task = entityUtil.getTaskById(taskId);

        task.setDropboxTaskFolderId(taskFolderId);
        updateTaskStatusAccordingToDate(task);
    }

    @Override
    @Transactional(readOnly = true)
    public int getTaskProgress(@NonNull Long taskId) {
        return taskRepository.findProgressById(taskId).orElseThrow(() ->
                new EntityNotFoundException("Task " + taskId + " was not found.", ErrorType.TASK_NOT_FOUND));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void subtaskUpdateListener(SubtasksChanged event) {
        final Task task = entityUtil.getTaskById(event.taskId());

        updateTaskProgress(task);
    }

    private void updateTaskProgress(Task task) {
        if (task.getStatus() == TaskStatus.COMPLETED) {
            task.setProgress(100);
            eventPublisher.publishEvent(new TaskProgressUpdated(task.getProject().getId()));
            return;
        }
        final int numberOfSubtasks = subtaskRepository.countByTaskId(task.getId());
        final int numberOfCompletedSubtasks = subtaskRepository.countByTaskIdAndIsCompletedTrue(task.getId());

        if (numberOfSubtasks == 0) {
            task.setProgress(0);
        } else {
            task.setProgress(numberOfCompletedSubtasks * 100 / numberOfSubtasks);
        }

        eventPublisher.publishEvent(new TaskProgressUpdated(task.getProject().getId()));
    }

    private void updateTaskStatusAccordingToDate(Task task) {
        if (task.getStatus().equals(TaskStatus.COMPLETED)) {
            return;
        }

        if (task.getDueDate() != null 
                && task.getDueDate().isBefore(LocalDate.now())) {
            task.setStatus(TaskStatus.OVERDUE);
        }
        if ((task.getDueDate() != null && task.getDueDate().isAfter(LocalDate.now())
                && !task.getStatus().equals(TaskStatus.NOT_STARTED)
                || (task.getDueDate() == null && task.getStatus().equals(TaskStatus.OVERDUE)))) {
            task.setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    private void checkDateIsLegit(Task task) {
        if (task.getDueDate() != null) {
            if (task.getProject().getEndDate() == null) {
                if (!task.getDueDate().isAfter(task.getProject().getStartDate())) {
                    throw new InconsistentDataException("Task's due date is specified as "
                            + task.getDueDate() + " in project where start date is currently "
                            + task.getProject().getStartDate()
                            + ". Task's due date must be after project's start date.",
                            ErrorType.TASK_DATE_BEFORE_PROJECT_START);
                }
            } else {
                if (!(task.getDueDate().isAfter(task.getProject().getStartDate())
                        && (task.getDueDate().isBefore(task.getProject().getEndDate())
                        || task.getDueDate().isEqual(task.getProject().getEndDate())))) {
                    throw new InconsistentDataException("Task's due date is specified as "
                            + task.getDueDate() + " in project where start and end dates "
                            + "currently are " + task.getProject().getStartDate() + " and "
                            + task.getProject().getEndDate() + " respectively. Task's due date "
                            + "must belong to the interval between project's "
                            + "start and end dates.", ErrorType.TASK_DATE_WRONG_INTERVAL);
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
                if (labelIds == null || labelIds.isEmpty()) return null;

                query.distinct(true);
                final Join<Task, Label> labelsJoin = root.join("labels");
                return labelsJoin.get("id").in(labelIds);
            };
        }

        public static Specification<Task> hasAllLabels(List<Long> labelIds) {
            return (root, query, cb) -> {
                if (labelIds == null || labelIds.isEmpty()) return null;

                final Set<Long> distinctIds = new HashSet<>(labelIds);

                final Subquery<Long> subquery = query.subquery(Long.class);
                final Root<Task> subTaskRoot = subquery.from(Task.class);
                final Join<Task, Label> subLabelsJoin = subTaskRoot.join("labels");

                subquery.select(subTaskRoot.get("id"));
                subquery.where(cb.and(cb.equal(root.get("id"), subTaskRoot.get("id")),
                        subLabelsJoin.get("id").in(distinctIds)));
                subquery.groupBy(subTaskRoot.get("id"));
                subquery.having(cb.equal(cb.countDistinct(subLabelsJoin.get("id")), distinctIds.size()));

                return cb.exists(subquery);
            };
        }
    }
}
