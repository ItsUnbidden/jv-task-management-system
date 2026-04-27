package com.unbidden.jvtaskmanagementsystem.exception.thirdparty.dropbox;

import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.FileOperationResult;
import com.unbidden.jvtaskmanagementsystem.exception.ErrorType;
import com.unbidden.jvtaskmanagementsystem.exception.thirdparty.ThirdPartyApiException;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FileUploadFailedException extends ThirdPartyApiException {
    private final FileOperationResult dropboxResult;

    public FileUploadFailedException(String message, ErrorType type,
            FileOperationResult dropboxResult) {
        super(message, type);
        this.dropboxResult = dropboxResult;
    }
}
