package com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.ThirdPartyOperationResult;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DropboxOperationResult extends ThirdPartyOperationResult {
    private final String errorMessage;
    
    public DropboxOperationResult(@NonNull ThirdPartyOperationStatus status, @Nullable String errorMessage) {
        super(status);
        this.errorMessage = errorMessage;
    }

    public DropboxOperationResult(@NonNull ThirdPartyOperationStatus status) {
        super(status);
        this.errorMessage = null;
    }
}
