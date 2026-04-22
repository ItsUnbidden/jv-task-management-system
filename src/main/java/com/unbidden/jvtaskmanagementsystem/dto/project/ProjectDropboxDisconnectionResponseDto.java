package com.unbidden.jvtaskmanagementsystem.dto.project;

import org.springframework.lang.NonNull;

import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.ThirdPartyOperationResult;

import lombok.Data;

@Data
public class ProjectDropboxDisconnectionResponseDto {
    @NonNull
    private ProjectResponseDto project;
    
    @NonNull
    private ThirdPartyOperationResult dropboxFolderDeleted;
}
