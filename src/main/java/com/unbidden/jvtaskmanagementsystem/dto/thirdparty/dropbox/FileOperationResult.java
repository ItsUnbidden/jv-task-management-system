package com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import com.dropbox.core.v2.files.FileMetadata;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FileOperationResult extends DropboxOperationResult {
    private final FileMetadata meta;

    private final FileOperationErrorTag tag;

    public FileOperationResult(@NonNull ThirdPartyOperationStatus status,
            @Nullable FileOperationErrorTag tag, @Nullable String errorMessage) {
        super(status, errorMessage);
        this.tag = tag;
        this.meta = null;
    }
    
    public FileOperationResult(@NonNull ThirdPartyOperationStatus status) {
        super(status);
        this.meta = null;
        this.tag = null;
    }

    public FileOperationResult(@NonNull ThirdPartyOperationStatus status, @Nullable FileMetadata meta) {
        super(status);
        this.meta = meta;
        this.tag = null;
    }

    public static enum FileOperationErrorTag {
        NO_TASK_FOLDER_ID,
        IOEXCEPTION,
        UNKNOWN
    }
}
