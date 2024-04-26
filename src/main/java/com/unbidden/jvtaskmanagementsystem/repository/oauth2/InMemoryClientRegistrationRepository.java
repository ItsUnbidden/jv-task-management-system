package com.unbidden.jvtaskmanagementsystem.repository.oauth2;

import com.unbidden.jvtaskmanagementsystem.exception.OAuth2PropertiesParsingException;
import com.unbidden.jvtaskmanagementsystem.model.ClientRegistration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
@SuppressWarnings("null")
public class InMemoryClientRegistrationRepository implements ClientRegistrationRepository {
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
        String providersStr = environment.getProperty("oauth2.providers");
        if (providersStr != null && !providersStr.isEmpty()) {
            String[] providers = providersStr.split(",");

            for (String provider : providers) {
                ClientRegistration clientRegistration = mapPropertiesForProvider(environment,
                        provider);
                registrations.put(clientRegistration.getClientName(), clientRegistration);
            }
        }
    }

    private ClientRegistration mapPropertiesForProvider(Environment environment,
            String provider) {
        final String basePath = "oauth2." + provider + ".";

        ClientRegistration clientRegistration = new ClientRegistration();
        clientRegistration.setClientName(provider);

        String clientId = environment.getProperty(basePath + "client-id");
        if (clientId == null) {
            throw new OAuth2PropertiesParsingException("Client id cannot be null.");
        }
        clientRegistration.setClientId(clientId);
        
        String clientSecret = environment.getProperty(basePath + "client-secret");
        if (clientSecret == null) {
            throw new OAuth2PropertiesParsingException("Client secret cannot be null.");
        }
        clientRegistration.setClientSecret(clientSecret);

        String redirectUri = environment.getProperty(basePath + "redirect-uri");
        if (redirectUri == null) {
            throw new OAuth2PropertiesParsingException("Redirect URI cannot be null.");
        }
        clientRegistration.setRedirectUri(redirectUri);

        String tokenUri = environment.getProperty(basePath + "token-uri");
        if (tokenUri == null) {
            throw new OAuth2PropertiesParsingException("Token URI cannot be null.");
        }
        clientRegistration.setTokenUri(tokenUri);

        String authorizationUri = environment.getProperty(basePath + "authorization-uri");
        if (authorizationUri == null) {
            throw new OAuth2PropertiesParsingException("Authorization URI " 
                    + "cannot be null.");
        }
        clientRegistration.setAuthorizationUri(authorizationUri);
        
        clientRegistration.setUseRefreshTokens(Boolean.valueOf(environment.getProperty(
                basePath + "use-refresh-tokens")));

        String defaultRedirectAfterCallback = environment.getProperty(basePath 
                + "default-redirect-after-callback");
        if (defaultRedirectAfterCallback == null) {
            defaultRedirectAfterCallback = "/users/me";
        }
        clientRegistration.setDefaultRedirectAfterCallback(defaultRedirectAfterCallback);
        return clientRegistration;
    }
}
