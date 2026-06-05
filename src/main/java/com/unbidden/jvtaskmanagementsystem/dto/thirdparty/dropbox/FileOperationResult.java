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

    public FileOperationResult(@NonNull ThirdPartyOperationStatus status,
            @Nullable DropboxErrorTag tag, @Nullable String errorMessage) {
        super(status, tag, errorMessage);
        this.meta = null;
    }
    
    public FileOperationResult(@NonNull ThirdPartyOperationStatus status) {
        super(status);
        this.meta = null;
    }

    public FileOperationResult(@NonNull ThirdPartyOperationStatus status, @Nullable FileMetadata meta) {
        super(status);
        this.meta = meta;
    }
}
