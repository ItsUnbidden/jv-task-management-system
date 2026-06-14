package com.unbidden.jvtaskmanagementsystem.dto.task;

import org.springframework.lang.NonNull;

import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.ThirdPartyOperationResult;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.DeleteResult;

import lombok.Data;

@Data
public class DeleteTaskResponseDto {
    @NonNull
    private String taskName;

    @NonNull
    private DeleteResult dropboxFolderDeleted;

    @NonNull
    private ThirdPartyOperationResult calendarDeleted;
}
