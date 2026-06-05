package com.unbidden.jvtaskmanagementsystem.exception.thirdparty.dropbox;

import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.FileOperationResult;
import com.unbidden.jvtaskmanagementsystem.exception.ErrorType;

public class FileDownloadException extends FileOperationException {
    public FileDownloadException(String message, ErrorType type,
            FileOperationResult result) {
        super(message, type, result);
    }
}
