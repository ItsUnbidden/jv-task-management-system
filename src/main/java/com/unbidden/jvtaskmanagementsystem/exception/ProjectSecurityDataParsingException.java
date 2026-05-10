package com.unbidden.jvtaskmanagementsystem.exception;

public class ProjectSecurityDataParsingException extends UnexpectedException {
    public ProjectSecurityDataParsingException(String msg, ErrorType type) {
        super(msg, type);
    }
}
