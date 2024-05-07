package com.unbidden.jvtaskmanagementsystem.service.oauth2.resolver;

import com.unbidden.jvtaskmanagementsystem.dto.oauth2.OAuth2TokenResponseDto;
import com.unbidden.jvtaskmanagementsystem.model.ClientRegistration;
import com.unbidden.jvtaskmanagementsystem.model.OAuth2AuthorizedClient;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.repository.oauth2.AuthorizedClientRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class OAuth2AuthorizedClientAbstractResolver
        implements OAuth2AuthorizedClientResolver {
    private static final Logger LOGGER =
            LogManager.getLogger(OAuth2AuthorizedClientAbstractResolver.class);

    protected ClientRegistration clientRegistration;

    protected AuthorizedClientRepository authorizedClientRepository;

    public OAuth2AuthorizedClientAbstractResolver(ClientRegistration clientRegistration,
            AuthorizedClientRepository authorizedClientRepository) {
        this.clientRegistration = clientRegistration;
        this.authorizedClientRepository = authorizedClientRepository;
    }

    @Override
    public OAuth2AuthorizedClient resolveAuthorizedClient(User user,
            OAuth2TokenResponseDto tokenData,
            Optional<OAuth2AuthorizedClient> authorizedClientOpt) {
        OAuth2AuthorizedClient authorizedClient;
        if (authorizedClientOpt.isEmpty()) {
            authorizedClient = new OAuth2AuthorizedClient();
            LOGGER.info("Created authorized client.");
        } else {
            authorizedClient = authorizedClientOpt.get();
            LOGGER.info("Loaded existing authorized client.");
        }

        authorizedClient.setAquiredAt(LocalDateTime.now());
        authorizedClient.setClientRegistrationName(clientRegistration.getClientName());
        authorizedClient.setUser(user);

        authorizedClient.setToken(tokenData.getAccessToken());
        authorizedClient.setExpiresIn(tokenData.getExpiresIn());
        LOGGER.info("Authorized client set up.");

        if (tokenData.getRefreshToken() != null) {
            authorizedClient.setRefreshToken(tokenData.getRefreshToken());
            LOGGER.info("Refresh token included.");
        }

        LOGGER.info("Initiating save.");
        return authorizedClientRepository.save(authorizedClient);
    }

    @Override
    public ClientRegistration getClientRegistration() {
        return clientRegistration;
    }
}
