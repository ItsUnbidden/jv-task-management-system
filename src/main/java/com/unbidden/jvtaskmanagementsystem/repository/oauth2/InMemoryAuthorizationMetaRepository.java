package com.unbidden.jvtaskmanagementsystem.repository.oauth2;

import com.unbidden.jvtaskmanagementsystem.model.AuthorizationMeta;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryAuthorizationMetaRepository implements AuthorizationMetaRepository {
    private static final Logger LOGGER =
            LogManager.getLogger(InMemoryAuthorizationMetaRepository.class);

    private static final int EXPIRATION_SECONDS = 300;

    private Map<String, AuthorizationMeta> metas = new HashMap<>();

    @Override
    public AuthorizationMeta save(AuthorizationMeta meta) {
        removeOverdue();
        metas.put(meta.getId().toString(), meta);
        LOGGER.info("Meta " + meta.getId() + " persisted.");
        return meta;
    }

    @Override
    public Optional<AuthorizationMeta> load(String id) {
        removeOverdue();
        return Optional.ofNullable(metas.get(id));
    }

    private void removeOverdue() {
        LOGGER.info("Checking for outdated metas...");
        List<String> keysForRemoval = new ArrayList<>();

        for (Entry<String, AuthorizationMeta> entry : metas.entrySet()) {
            if (entry.getValue().getCreatedAt().plusSeconds(EXPIRATION_SECONDS)
                    .isBefore(LocalDateTime.now())) {
                keysForRemoval.add(entry.getKey());
            }
        }
        
        if (keysForRemoval.isEmpty()) {
            LOGGER.info("No metas to remove.");
        } else {
            LOGGER.info("Some metas are overdue.");
        }

        for (String key : keysForRemoval) {
            metas.remove(key);
            LOGGER.info("Removed meta " + key);
        }
    }
}
