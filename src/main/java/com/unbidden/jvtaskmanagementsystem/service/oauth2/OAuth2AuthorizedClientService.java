package com.unbidden.jvtaskmanagementsystem.service.oauth2;

import java.util.Optional;

import org.springframework.lang.NonNull;

import com.unbidden.jvtaskmanagementsystem.dto.oauth2.OAuth2TokenResponseDto;
import com.unbidden.jvtaskmanagementsystem.model.ClientRegistration;
import com.unbidden.jvtaskmanagementsystem.model.OAuth2AuthorizedClient;
import com.unbidden.jvtaskmanagementsystem.model.User;

public interface OAuth2AuthorizedClientService {
    @NonNull
    Optional<OAuth2AuthorizedClient> findClient(@NonNull User user,
            @NonNull ClientRegistration registration);

    boolean isExternalIdTaken(@NonNull String externalId);
    
    @NonNull
    OAuth2AuthorizedClient register(@NonNull User user,
            @NonNull OAuth2TokenResponseDto tokenData,
            @NonNull ClientRegistration clientRegistration,
            @NonNull String externalId);
    
    @NonNull
    OAuth2AuthorizedClient update(@NonNull Long clientId,
            @NonNull OAuth2TokenResponseDto tokenData);

    void delete(@NonNull Long id);
}
