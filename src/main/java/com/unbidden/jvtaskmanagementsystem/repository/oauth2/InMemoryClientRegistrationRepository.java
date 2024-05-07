package com.unbidden.jvtaskmanagementsystem.repository.oauth2;

import com.unbidden.jvtaskmanagementsystem.exception.oauth2.OAuth2PropertiesParsingException;
import com.unbidden.jvtaskmanagementsystem.model.ClientRegistration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
@SuppressWarnings("null")
public class InMemoryClientRegistrationRepository implements ClientRegistrationRepository {
    private static final Logger LOGGER =
            LogManager.getLogger(InMemoryClientRegistrationRepository.class);

    private Map<String, ClientRegistration> registrations = new HashMap<>();

    public InMemoryClientRegistrationRepository(@Autowired Environment environment) {
        initializeClientRegistrations(environment);
    }

    @Override
    @NonNull
    public Optional<ClientRegistration> findByClientName(@NonNull String name) {
        return Optional.ofNullable(registrations.get(name));
    }

    private void initializeClientRegistrations(Environment environment) {
        LOGGER.info("Attempting to initialize client registrations.");
        String providersStr = environment.getProperty("oauth2.providers");

        if (providersStr != null && !providersStr.isEmpty()) {
            String[] providers = providersStr.split(",");

            for (String provider : providers) {
                ClientRegistration clientRegistration = mapPropertiesForProvider(environment,
                        provider);
                registrations.put(clientRegistration.getClientName(), clientRegistration);
            }
        } else {
            LOGGER.warn("No client registrations are present in 'application.properties'. "
                    + "No third party services will be available.");
        }
    }

    private ClientRegistration mapPropertiesForProvider(Environment environment,
            String provider) {
        final String basePath = "oauth2." + provider + ".";

        LOGGER.info("Initiating properties mapping for " + provider);
        ClientRegistration clientRegistration = new ClientRegistration();
        clientRegistration.setClientName(provider);

        String clientId = environment.getProperty(basePath + "client-id");
        if (clientId == null) {
            throw new OAuth2PropertiesParsingException("Client id cannot be null.");
        }
        clientRegistration.setClientId(clientId);
        LOGGER.info("Client id for " + provider + " is set to " + clientId);
        
        String clientSecret = environment.getProperty(basePath + "client-secret");
        if (clientSecret == null) {
            throw new OAuth2PropertiesParsingException("Client secret cannot be null.");
        }
        clientRegistration.setClientSecret(clientSecret);
        LOGGER.info("Client secret for " + provider + " is set.");

        String redirectUri = environment.getProperty(basePath + "redirect-uri");
        if (redirectUri == null) {
            throw new OAuth2PropertiesParsingException("Redirect URI cannot be null.");
        }
        clientRegistration.setRedirectUri(redirectUri);
        LOGGER.info("Redirect URI for " + provider + " is set to " + redirectUri);

        String tokenUri = environment.getProperty(basePath + "token-uri");
        if (tokenUri == null) {
            throw new OAuth2PropertiesParsingException("Token URI cannot be null.");
        }
        clientRegistration.setTokenUri(tokenUri);
        LOGGER.info("Token URI for " + provider + " is set to " + tokenUri);

        String authorizationUri = environment.getProperty(basePath + "authorization-uri");
        if (authorizationUri == null) {
            throw new OAuth2PropertiesParsingException("Authorization URI " 
                    + "cannot be null.");
        }
        clientRegistration.setAuthorizationUri(authorizationUri);
        LOGGER.info("Authorization URI for " + provider + " is set to " + authorizationUri);
        
        clientRegistration.setUseRefreshTokens(Boolean.valueOf(environment.getProperty(
                basePath + "use-refresh-tokens")));
        LOGGER.info("Use refresh tokens for " + provider + " is set to "
                + clientRegistration.getUseRefreshTokens());

        return clientRegistration;
    }
}
