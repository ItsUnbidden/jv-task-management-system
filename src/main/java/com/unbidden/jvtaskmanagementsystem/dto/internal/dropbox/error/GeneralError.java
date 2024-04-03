package com.unbidden.jvtaskmanagementsystem.dto.internal.dropbox.error;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.unbidden.jvtaskmanagementsystem.dto.internal.dropbox.DropboxResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class GeneralError extends DropboxResponse {
    private Error error;

    @JsonProperty("error_summary")
    private String errorSummary;

    public static class Error {
        @JsonProperty(".tag")
        private String tag;
    }
}
