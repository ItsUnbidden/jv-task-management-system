package com.unbidden.jvtaskmanagementsystem.exception;

public class FileSizeLimitExceededException extends UnexpectedException {
    public FileSizeLimitExceededException(String message, ErrorType type) {
        super(message, type);
    }
}
