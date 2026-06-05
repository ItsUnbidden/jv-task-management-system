package com.unbidden.jvtaskmanagementsystem.exception.thirdparty.dropbox;

import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.DropboxOperationResult;
import com.unbidden.jvtaskmanagementsystem.exception.ErrorType;
import com.unbidden.jvtaskmanagementsystem.exception.thirdparty.ThirdPartyApiException;

public class FileOperationException extends ThirdPartyApiException {
    public FileOperationException(String message, ErrorType type,
            DropboxOperationResult result) {
        super(message, type, result);
    }
}
