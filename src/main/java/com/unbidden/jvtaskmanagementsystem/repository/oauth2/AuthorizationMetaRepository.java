package com.unbidden.jvtaskmanagementsystem.repository.oauth2;

import com.unbidden.jvtaskmanagementsystem.model.AuthorizationMeta;
import java.util.Optional;

public interface AuthorizationMetaRepository {
    AuthorizationMeta save(AuthorizationMeta meta);

    Optional<AuthorizationMeta> load(String id);
}
