package com.unbidden.jvtaskmanagementsystem.service;

import com.unbidden.jvtaskmanagementsystem.dto.label.CreateLabelRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.label.LabelResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.label.UpdateLabelRequestDto;
import com.unbidden.jvtaskmanagementsystem.model.User;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;

public interface LabelService {

    List<LabelResponseDto> getLablesForProject(User user,
            @NonNull Long projectId, Pageable pageable);

    LabelResponseDto getLabelById(User user, @NonNull Long labelId);

    LabelResponseDto createLabel(User user, @NonNull Long projectId,
            @NonNull CreateLabelRequestDto requestDto);

    LabelResponseDto updateLabel(User user, @NonNull Long labelId,
            @NonNull UpdateLabelRequestDto requestDto);

    void deleteLabel(User user, @NonNull Long labelId);

}
