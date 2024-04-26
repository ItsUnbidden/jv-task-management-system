package com.unbidden.jvtaskmanagementsystem.repository.oauth2;

import com.unbidden.jvtaskmanagementsystem.model.OAuth2AuthorizedClient;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

public interface AuthorizedClientRepository extends JpaRepository<OAuth2AuthorizedClient, Long> {
    @NonNull
    @Query("from OAuth2AuthorizedClient ac left join fetch ac.user u where u.id = " 
            + ":userId and ac.clientRegistrationName = :registrationName")
    Optional<OAuth2AuthorizedClient> findByUserIdAndRegistrationName(@NonNull Long userId,
            String registrationName);

    @EntityGraph(attributePaths = "user")
    Optional<OAuth2AuthorizedClient> findByExternalAccountId(String externalAccountId);
}
