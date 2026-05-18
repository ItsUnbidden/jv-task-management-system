package com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox;

import org.springframework.lang.NonNull;

import com.dropbox.core.v2.files.FileMetadata;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FileUploadOperationResult extends FileOperationResult {
    private final CreatedTaskFolderResult newFolderResult;

    public FileUploadOperationResult(@NonNull ThirdPartyOperationStatus status,
            FileOperationErrorTag tag, String errorMessage) {
        super(status, tag, errorMessage);
        this.newFolderResult = null;
    }
    
    public FileUploadOperationResult(@NonNull ThirdPartyOperationStatus status) {
        super(status);
        this.newFolderResult = null;
    }

    public FileUploadOperationResult(@NonNull ThirdPartyOperationStatus status, FileMetadata meta,
            CreatedTaskFolderResult newFolderResult) {
        super(status, meta);
        this.newFolderResult = newFolderResult; 
    }
}
