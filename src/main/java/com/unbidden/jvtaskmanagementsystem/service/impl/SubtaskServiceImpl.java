package com.unbidden.jvtaskmanagementsystem.service.impl;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unbidden.jvtaskmanagementsystem.dto.internal.SubtasksChanged;
import com.unbidden.jvtaskmanagementsystem.dto.task.CreateSubtaskRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.SubtaskResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.UpdateSubtaskRequestDto;
import com.unbidden.jvtaskmanagementsystem.mapper.TaskMapper;
import com.unbidden.jvtaskmanagementsystem.model.ProjectRole.ProjectRoleType;
import com.unbidden.jvtaskmanagementsystem.model.Subtask;
import com.unbidden.jvtaskmanagementsystem.model.Task;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.repository.SubtaskRepository;
import com.unbidden.jvtaskmanagementsystem.security.project.ProjectSecurity;
import com.unbidden.jvtaskmanagementsystem.service.SubtaskService;
import com.unbidden.jvtaskmanagementsystem.util.EntityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubtaskServiceImpl implements SubtaskService {
    private final ApplicationEventPublisher eventPublisher;

    private final SubtaskRepository subtaskRepository;

    private final TaskMapper taskMapper;

    private final EntityUtil entityUtil;

    @NonNull
    @Override
    @Transactional(readOnly = true)
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, entityIdClass = Task.class, bypassIfPublic = true)
    public Page<SubtaskResponseDto> getSubtasksByTaskId(@NonNull User user, @NonNull Long taskId, @NonNull Pageable pageable) {
        return subtaskRepository.findByTaskId(taskId, pageable).map(taskMapper::toSubtaskDto);
    }

    @NonNull
    @Override
    @Transactional
    @ProjectSecurity(securityLevel = ProjectRoleType.ADMIN, entityIdClass = Task.class)
    public SubtaskResponseDto createSubtask(@NonNull User user, @NonNull Long taskId, @NonNull CreateSubtaskRequestDto requestDto) {
        final Task task = entityUtil.getTaskById(taskId);
        final Subtask subtask = new Subtask();
        
        eventPublisher.publishEvent(new SubtasksChanged(task.getId()));

        subtask.setName(requestDto.name());
        subtask.setTask(task);
        return taskMapper.toSubtaskDto(subtaskRepository.save(subtask));
    }

    @NonNull
    @Override
    @Transactional
    @ProjectSecurity(securityLevel = ProjectRoleType.ADMIN, entityIdClass = Subtask.class)
    public SubtaskResponseDto updateSubtask(@NonNull User user, @NonNull Long subtaskId, @NonNull UpdateSubtaskRequestDto requestDto) {
        final Subtask subtask = entityUtil.getSubtaskById(subtaskId);

        if (subtask.isCompleted() != requestDto.isCompleted()) {
            eventPublisher.publishEvent(new SubtasksChanged(subtask.getTask().getId()));
        }

        subtask.setName(requestDto.name());
        subtask.setCompleted(requestDto.isCompleted());
        return taskMapper.toSubtaskDto(subtask);
    }

    @Override
    @Transactional
    @ProjectSecurity(securityLevel = ProjectRoleType.ADMIN, entityIdClass = Subtask.class)
    public void deleteSubtask(@NonNull User user, @NonNull Long subtaskId) {
        final Subtask subtask = entityUtil.getSubtaskById(subtaskId);
        
        eventPublisher.publishEvent(new SubtasksChanged(subtask.getTask().getId()));

        subtaskRepository.delete(subtask);
    }
}
