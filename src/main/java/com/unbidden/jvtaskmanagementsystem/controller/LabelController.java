package com.unbidden.jvtaskmanagementsystem.controller;

import com.unbidden.jvtaskmanagementsystem.dto.label.CreateLabelRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.label.LabelResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.label.UpdateLabelRequestDto;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.service.LabelService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
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

@RestController
@RequestMapping("/labels")
@RequiredArgsConstructor
@SuppressWarnings("null")
public class LabelController {
    private final LabelService labelService;

    @GetMapping("/projects/{projectId}")
    public List<LabelResponseDto> getLabelsForProject(Authentication authentication,
            @NonNull @PathVariable Long projectId, Pageable pageable) {
        return labelService.getLablesForProject((User)authentication.getPrincipal(),
                projectId, pageable);
    }
    
    @GetMapping("/{labelId}")
    public LabelResponseDto getLabelById(Authentication authentication,
            @NonNull @PathVariable Long labelId) {
        return labelService.getLabelById((User)authentication.getPrincipal(), labelId);
    }
    
    @PostMapping()
    public LabelResponseDto createLabel(Authentication authentication,
            @NonNull @RequestBody @Valid CreateLabelRequestDto requestDto) {
        return labelService.createLabel((User)authentication.getPrincipal(),
                requestDto.getProjectId(), requestDto);
    }
    
    @PutMapping("/{labelId}")
    public LabelResponseDto updateLabel(Authentication authentication,
            @NonNull @PathVariable Long labelId,
            @NonNull @RequestBody @Valid UpdateLabelRequestDto requestDto) {
        return labelService.updateLabel((User)authentication.getPrincipal(),
                labelId, requestDto);
    }

    @DeleteMapping("/{labelId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLabel(Authentication authentication, @NonNull @PathVariable Long labelId) {
        labelService.deleteLabel((User)authentication.getPrincipal(), labelId);
    }
}
