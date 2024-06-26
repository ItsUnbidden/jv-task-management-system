package com.unbidden.jvtaskmanagementsystem.service.impl;

import com.unbidden.jvtaskmanagementsystem.dto.label.CreateLabelRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.label.LabelResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.label.UpdateLabelRequestDto;
import com.unbidden.jvtaskmanagementsystem.mapper.LabelMapper;
import com.unbidden.jvtaskmanagementsystem.model.Label;
import com.unbidden.jvtaskmanagementsystem.model.Project;
import com.unbidden.jvtaskmanagementsystem.model.ProjectRole.ProjectRoleType;
import com.unbidden.jvtaskmanagementsystem.model.Task;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.repository.LabelRepository;
import com.unbidden.jvtaskmanagementsystem.repository.TaskRepository;
import com.unbidden.jvtaskmanagementsystem.security.project.ProjectSecurity;
import com.unbidden.jvtaskmanagementsystem.service.LabelService;
import com.unbidden.jvtaskmanagementsystem.util.EntityUtil;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LabelServiceImpl implements LabelService {
    private final EntityUtil entityUtil;

    private final LabelRepository labelRepository;

    private final LabelMapper labelMapper;

    private final TaskRepository taskRepository;

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, bypassIfPublic = true)
    public List<LabelResponseDto> getLablesForProject(@NonNull User user, @NonNull Long projectId,
            Pageable pageable) {
        return labelRepository.findByProjectId(projectId).stream()
                .map(labelMapper::toDto)
                .toList();
    }

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.CONTRIBUTOR, bypassIfPublic = true,
            entityIdClass = Label.class)
    public LabelResponseDto getLabelById(@NonNull User user, @NonNull Long labelId) {
        return labelMapper.toDto(entityUtil.getLabelById(labelId));
    }

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.ADMIN)
    public LabelResponseDto createLabel(@NonNull User user, @NonNull Long projectId,
            @NonNull CreateLabelRequestDto requestDto) {
        final Label label = labelMapper.toModel(requestDto);
        final Project project = entityUtil.getProjectById(projectId);
        final List<Task> tasks = taskRepository.findAllById(requestDto.getTaskIds());

        label.setProject(project);
        tasks.forEach(t -> t.getLabels().add(label));
        label.setTasks(new HashSet<>(tasks));
        return labelMapper.toDto(labelRepository.save(label));
    }

    @NonNull
    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.ADMIN,
            entityIdClass = Label.class)
    public LabelResponseDto updateLabel(@NonNull User user, @NonNull Long labelId,
            @NonNull UpdateLabelRequestDto requestDto) {
        final Label label = entityUtil.getLabelById(labelId);
        
        label.setName(requestDto.getName());
        label.setColor(requestDto.getColor());
        
        List<Task> tasksFromRequest = taskRepository.findAllById(requestDto.getTaskIds());
        for (Task task : label.getTasks()) {
            if (!tasksFromRequest.contains(task)) {
                task.getLabels().remove(label);
            }
        }
        tasksFromRequest.forEach(t -> t.getLabels().add(label));
        label.setTasks(new HashSet<>(tasksFromRequest));
        
        return labelMapper.toDto(labelRepository.save(label));
    }

    @Override
    @ProjectSecurity(securityLevel = ProjectRoleType.ADMIN,
            entityIdClass = Label.class)
    public void deleteLabel(@NonNull User user, @NonNull Long labelId) {
        labelRepository.deleteById(labelId);
    }
}
