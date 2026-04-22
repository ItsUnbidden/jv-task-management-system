package com.unbidden.jvtaskmanagementsystem.dto.project;

import org.springframework.lang.NonNull;

import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.ThirdPartyOperationResult;

import lombok.Data;

@Data
public class ProjectCalendarDisconnectionResponseDto {
    @NonNull
    private ProjectResponseDto project;

    @NonNull
    private ThirdPartyOperationResult calendarDeleted;
}
