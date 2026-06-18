package com.unbidden.jvtaskmanagementsystem.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;

import com.unbidden.jvtaskmanagementsystem.dto.task.CreateSubtaskRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.SubtaskResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.UpdateSubtaskRequestDto;
import com.unbidden.jvtaskmanagementsystem.model.User;

public interface SubtaskService {
    @NonNull
    Page<SubtaskResponseDto> getSubtasksByTaskId(@NonNull User user, @NonNull Long taskId, @NonNull Pageable pageable);

    @NonNull
    SubtaskResponseDto createSubtask(@NonNull User user, @NonNull Long taskId, @NonNull CreateSubtaskRequestDto requestDto);

    @NonNull
    SubtaskResponseDto updateSubtask(@NonNull User user, @NonNull Long subtaskId, @NonNull UpdateSubtaskRequestDto requestDto);

    void deleteSubtask(@NonNull User user, @NonNull Long subtaskId);
}
