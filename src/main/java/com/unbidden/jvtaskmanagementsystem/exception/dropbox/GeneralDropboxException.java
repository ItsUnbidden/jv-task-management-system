package com.unbidden.jvtaskmanagementsystem.exception.dropbox;

public class GeneralDropboxException extends DropboxException {
    public GeneralDropboxException(String msg) {
        super(msg);
    }

    public GeneralDropboxException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
