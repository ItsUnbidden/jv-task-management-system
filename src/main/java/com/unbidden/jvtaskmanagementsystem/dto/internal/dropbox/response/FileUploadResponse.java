package com.unbidden.jvtaskmanagementsystem.dto.internal.dropbox.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FileUploadResponse extends SearchFileResponse {
    @JsonProperty("file_lock_info")
    private FileLockMetadata fileLockInfo;
    
    @JsonProperty("property_groups")
    private List<PropertyGroup> propertyGroups;

    @Data
    public static class FileLockMetadata {
        @JsonProperty("is_lockholder")
        private Boolean isLockholder;
        
        @JsonProperty("lockholder_name")
        private String lockholderName;
        
        @JsonProperty("lockholder_account_id")
        private String lockholderAccountId;

        private LocalDateTime created;
    }

    @Data
    public static class PropertyGroup {
        @JsonProperty("template_id")
        private String templateId;

        private List<PropertyField> fields;

        @Data
        public static class PropertyField {
            private String name;

            private String value;
        }
    }
}
