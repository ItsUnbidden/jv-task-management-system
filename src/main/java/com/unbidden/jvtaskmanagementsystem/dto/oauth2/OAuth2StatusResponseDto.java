package com.unbidden.jvtaskmanagementsystem.dto.oauth2;

import java.time.LocalDateTime;

import org.springframework.lang.NonNull;

import lombok.Data;

@Data
public class OAuth2StatusResponseDto {
    @NonNull
    private OAuth2Status status;

    private LocalDateTime aquiredAt;

    public OAuth2StatusResponseDto(@NonNull OAuth2Status status, LocalDateTime aquiredAt) {
        this.status = status;
        this.aquiredAt = aquiredAt;
    }

    public static enum OAuth2Status {
        OK,
        EXPIRED,
        NOT_CONNECTED
    }
}
