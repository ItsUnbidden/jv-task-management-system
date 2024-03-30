package com.unbidden.jvtaskmanagementsystem.dto.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public abstract class AbstractDropboxResponseDto {
    private Error error;

    @JsonProperty("error_summary")
    private String errorSummary;

    public static class Error {
        @JsonProperty(".tag")
        private String tag;
    }
}
