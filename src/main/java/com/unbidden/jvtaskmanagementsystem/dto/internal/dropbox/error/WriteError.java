package com.unbidden.jvtaskmanagementsystem.dto.internal.dropbox.error;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class WriteError extends GeneralError {
    @JsonProperty("malformed_path")
    private String malformedPath;
    
    @JsonProperty("conflict")
    private WriteConflictError conflict;
    
    @JsonProperty("no_write_permission")
    private Void noWritePermission;
    
    @JsonProperty("insufficient_space")
    private Void insufficientSpace;
    
    @JsonProperty("disallowed_name")
    private Void disallowedName;
    
    @JsonProperty("team_folder")
    private Void teamFolder;
    
    @JsonProperty("operation_suppressed")
    private Void operationSuppressed;
    
    @JsonProperty("too_many_write_operations")
    private Void tooManyWriteOperations;
    
    @JsonProperty("access_restricted")
    private Void accessRestricted;

    @Data
    public static class WriteConflictError {
        @JsonProperty("file")
        private Void file;
        
        @JsonProperty("folder")
        private Void folder;
        
        @JsonProperty("file_ancestor")
        private Void fileAncestor;
    }
}
