package com.unbidden.jvtaskmanagementsystem.repository.oauth2;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import com.unbidden.jvtaskmanagementsystem.model.AuthorizationMeta;

@Repository
public class InMemoryAuthorizationMetaRepository implements AuthorizationMetaRepository {
    private static final Logger LOGGER =
            LogManager.getLogger(InMemoryAuthorizationMetaRepository.class);

    private static final int EXPIRATION_SECONDS = 300;

    private final ConcurrentMap<String, AuthorizationMeta> metas = new ConcurrentHashMap<>();

    @Override
    public AuthorizationMeta save(AuthorizationMeta meta) {
        removeOverdue();
        metas.put(meta.getId().toString(), meta);
        LOGGER.debug("Meta " + meta.getId() + " persisted.");
        return meta;
    }

    @Override
    public Optional<AuthorizationMeta> load(String id) {
        removeOverdue();
        return Optional.ofNullable(metas.get(id));
    }

    private void removeOverdue() {
        LOGGER.debug("Checking for outdated metas...");
        List<String> keysForRemoval = new ArrayList<>();

        for (Entry<String, AuthorizationMeta> entry : metas.entrySet()) {
            if (entry.getValue().getCreatedAt().plusSeconds(EXPIRATION_SECONDS)
                    .isBefore(LocalDateTime.now())) {
                keysForRemoval.add(entry.getKey());
            }
        }
        
        if (keysForRemoval.isEmpty()) {
            LOGGER.debug("No metas to remove.");
            return;
        } else {
            LOGGER.debug("Some metas are overdue.");
        }

        for (String key : keysForRemoval) {
            metas.remove(key);
            LOGGER.debug("Removed meta " + key);
        }
    }
}
