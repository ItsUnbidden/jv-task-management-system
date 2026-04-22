package com.unbidden.jvtaskmanagementsystem.dto.project;

import org.springframework.lang.NonNull;

import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.ThirdPartyOperationResult;

import lombok.Data;

@Data
public class DeleteProjectResponseDto {
    @NonNull
    private String projectName;
    
    @NonNull
    private ThirdPartyOperationResult isDropboxFolderDeleted;

    @NonNull
    private ThirdPartyOperationResult isCalendarDeleted;
}
