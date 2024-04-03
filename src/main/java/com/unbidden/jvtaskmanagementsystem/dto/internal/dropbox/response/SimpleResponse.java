package com.unbidden.jvtaskmanagementsystem.dto.internal.dropbox.response;

import com.unbidden.jvtaskmanagementsystem.dto.internal.dropbox.DropboxResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class SimpleResponse extends DropboxResponse {
    private String result;
}
