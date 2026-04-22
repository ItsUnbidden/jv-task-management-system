package com.unbidden.jvtaskmanagementsystem.dto.task;

import org.springframework.lang.NonNull;

import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.ThirdPartyOperationResult;

import lombok.Data;

@Data
public class DeleteTaskResponseDto {
    @NonNull
    private String taskName;

    @NonNull
    private ThirdPartyOperationResult dropboxFolderDeleted;

    @NonNull
    private ThirdPartyOperationResult calendarFolderDeleted;
}
