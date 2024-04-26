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

    public AuthorizationMeta(@NonNull UUID id, @NonNull User user, 
            @NonNull ClientRegistration clientRegistration, 
            @NonNull LocalDateTime createdAt) {
        this.id = id;
        this.user = user;
        this.clientRegistration = clientRegistration;
        this.createdAt = createdAt;
    }
}
