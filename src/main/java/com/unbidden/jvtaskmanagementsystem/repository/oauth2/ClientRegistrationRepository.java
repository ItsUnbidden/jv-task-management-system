package com.unbidden.jvtaskmanagementsystem.repository.oauth2;

import com.unbidden.jvtaskmanagementsystem.model.ClientRegistration;
import java.util.Optional;
import org.springframework.lang.NonNull;

public interface ClientRegistrationRepository {
    @NonNull
    Optional<ClientRegistration> findByClientName(@NonNull String name);
}
