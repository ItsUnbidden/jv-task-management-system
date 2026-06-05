package com.unbidden.jvtaskmanagementsystem.exception.thirdparty.dropbox;

import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.DeleteResult;
import com.unbidden.jvtaskmanagementsystem.exception.ErrorType;

public class FileDeleteException extends FileOperationException {
    public FileDeleteException(String message, ErrorType type, DeleteResult result) {
        super(message, type, result);
    }
}
