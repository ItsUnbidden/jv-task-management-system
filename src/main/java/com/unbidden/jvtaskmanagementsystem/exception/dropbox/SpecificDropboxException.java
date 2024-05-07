package com.unbidden.jvtaskmanagementsystem.exception.dropbox;

public class SpecificDropboxException extends DropboxException {
    public SpecificDropboxException(String msg) {
        super(msg);
    }

    public SpecificDropboxException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
