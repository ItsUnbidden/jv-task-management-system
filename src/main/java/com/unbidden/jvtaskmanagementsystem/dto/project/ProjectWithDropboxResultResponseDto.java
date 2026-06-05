package com.unbidden.jvtaskmanagementsystem.dto.project;

import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.DropboxOperationResult;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectWithDropboxResultResponseDto extends ProjectResponseDto {
    private DropboxOperationResult dropboxResult;
}
