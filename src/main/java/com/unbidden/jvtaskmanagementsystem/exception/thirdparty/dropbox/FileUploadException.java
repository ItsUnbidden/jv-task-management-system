package com.unbidden.jvtaskmanagementsystem.exception.thirdparty.dropbox;

import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.FileOperationResult;
import com.unbidden.jvtaskmanagementsystem.exception.ErrorType;

public class FileUploadException extends FileOperationException {
    public FileUploadException(String message, ErrorType type,
            FileOperationResult dropboxResult) {
        super(message, type, dropboxResult);
    }
}
