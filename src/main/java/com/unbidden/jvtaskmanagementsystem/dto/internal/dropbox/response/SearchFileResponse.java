package com.unbidden.jvtaskmanagementsystem.dto.internal.dropbox.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.unbidden.jvtaskmanagementsystem.dto.internal.dropbox.DropboxResponse;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class SearchFileResponse extends DropboxResponse {
    @JsonProperty("client_modified")
    private LocalDateTime clientModified;

    @JsonProperty("content_hash")
    private String contentHash;

    @JsonProperty("has_explicit_shared_members")
    private Boolean hasExplicitSharedMembers;

    private String id;

    @JsonProperty("is_downloadable")
    private Boolean isDownloadable;

    private String name;

    @JsonProperty("path_display")
    private String pathDisplay;

    @JsonProperty("path_lower")
    private String pathLower;

    private String rev;

    @JsonProperty("server_modified")
    private LocalDateTime serverModified;

    @JsonProperty("sharing_info")
    private FileSharingInfo sharingInfo;

    private Long size;

    public static class FileSharingInfo {
        @JsonProperty("is_downloadable")
        private Boolean readOnly;
        
        @JsonProperty("parent_shared_folder_id")
        private String parentSharedFolderId;
        
        @JsonProperty("modified_by")
        private String modifiedBy;
    }
}
