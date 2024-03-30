package com.unbidden.jvtaskmanagementsystem.model;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;
import org.springframework.lang.NonNull;

@Data
public class AuthorizationMeta {
    @NonNull
    private UUID id; 
    
    @NonNull
    private User user;
    
    @NonNull
    private ClientRegistration clientRegistration;

    @NonNull
    private LocalDateTime createdAt;

    private String origin;

    public AuthorizationMeta(@NonNull UUID id, @NonNull User user, 
            @NonNull ClientRegistration clientRegistration, 
            @NonNull LocalDateTime createdAt, String origin) {
        this.id = id;
        this.user = user;
        this.clientRegistration = clientRegistration;
        this.origin = origin;
        this.createdAt = createdAt;
    }
}
