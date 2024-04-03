package com.unbidden.jvtaskmanagementsystem.dto.internal.dropbox.error;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UploadError extends GeneralError {
    @JsonProperty("path")
    private UploadWriteFailed path;
    
    @JsonProperty("payload_too_large")
    private Void payloadTooLarge;
    
    @JsonProperty("content_hash_mismatch")
    private Void contentHashMismatch;

    @Data
    public static class UploadWriteFailed {
        @JsonProperty("reason")
        private WriteError reason;
        
        @JsonProperty("upload_session_id")
        private String uploadSessionId;
    }
}
