package com.unbidden.jvtaskmanagementsystem.repository.oauth2;

import com.unbidden.jvtaskmanagementsystem.model.AuthorizationMeta;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryAuthorizationMetaRepository implements AuthorizationMetaRepository {
    private static final int EXPIRATION_SECONDS = 300;

    private Map<String, AuthorizationMeta> metas = new HashMap<>();

    @Override
    public AuthorizationMeta save(AuthorizationMeta meta) {
        removeOverdue();
        metas.put(meta.getId().toString(), meta);
        return meta;
    }

    @Override
    public Optional<AuthorizationMeta> load(String id) {
        removeOverdue();
        return Optional.ofNullable(metas.get(id));
    }

    private void removeOverdue() {
        for (Entry<String, AuthorizationMeta> entry : metas.entrySet()) {
            if (entry.getValue().getCreatedAt().plusSeconds(EXPIRATION_SECONDS)
                    .isBefore(LocalDateTime.now())) {
                metas.remove(entry.getKey());
            }
        }
    }
}
