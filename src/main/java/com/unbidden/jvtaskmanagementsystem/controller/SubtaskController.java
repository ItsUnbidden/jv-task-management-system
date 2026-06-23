package com.unbidden.jvtaskmanagementsystem.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.unbidden.jvtaskmanagementsystem.dto.task.CreateSubtaskRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.SubtaskResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.UpdateSubtaskRequestDto;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.service.SubtaskService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
@RequestMapping("/subtasks")
public class SubtaskController {
    private final SubtaskService subtaskService;

    @GetMapping("/task/{id}")
    public Page<SubtaskResponseDto> getSubtasksByTaskId(Authentication authentication,
            @NonNull @PathVariable Long id, @NonNull Pageable pageable) {
        return subtaskService.getSubtasksByTaskId((User)authentication.getPrincipal(), id, pageable);
    }

    @PostMapping
    public SubtaskResponseDto createSubtask(Authentication authentication,
            @NonNull @Valid @RequestBody CreateSubtaskRequestDto requestDto) {
        return subtaskService.createSubtask((User)authentication.getPrincipal(), requestDto.taskId(), requestDto);
    }

    @PutMapping("/{id}")
    public SubtaskResponseDto updateSubtask(Authentication authentication, @NonNull @PathVariable Long id,
            @NonNull @Valid @RequestBody UpdateSubtaskRequestDto requestDto) {
        return subtaskService.updateSubtask((User)authentication.getPrincipal(), id, requestDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateSubtask(Authentication authentication, @NonNull @PathVariable Long id) {
        subtaskService.deleteSubtask((User)authentication.getPrincipal(), id);
    }
}
