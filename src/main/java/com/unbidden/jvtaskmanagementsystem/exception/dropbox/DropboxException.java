package com.unbidden.jvtaskmanagementsystem.exception.dropbox;

public abstract class DropboxException extends RuntimeException {
    public DropboxException(String msg) {
        super(msg);
    }

    public DropboxException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
