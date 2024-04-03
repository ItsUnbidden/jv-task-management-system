package com.unbidden.jvtaskmanagementsystem.dto.internal.dropbox.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.unbidden.jvtaskmanagementsystem.dto.internal.dropbox.DropboxResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class CreateFolderResponse extends DropboxResponse {
    @JsonProperty("metadata")
    private FolderMetadata metadata;

    @Data
    public static class FolderMetadata {
        
    }
}
