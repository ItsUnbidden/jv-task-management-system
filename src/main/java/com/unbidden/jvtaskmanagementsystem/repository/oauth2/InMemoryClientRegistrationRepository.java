package com.unbidden.jvtaskmanagementsystem.repository.oauth2;

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

    //TODO: pretty this up
    private void initializeClientRegistrations(Environment environment) {
        String[] providers = environment.getProperty("oauth2.providers").split(",");
        for (String provider : providers) {
            final String basePath = "oauth2." + provider + ".";

            ClientRegistration clientRegistration = new ClientRegistration();
            clientRegistration.setClientName(provider);
            clientRegistration.setClientId(environment.getProperty(basePath + "client-id"));
            clientRegistration.setClientSecret(environment.getProperty(basePath + "client-secret"));
            clientRegistration.setRedirectUri(environment.getProperty(basePath + "redirect-uri"));
            clientRegistration.setTokenUri(environment.getProperty(basePath + "token-uri"));
            clientRegistration.setAuthorizationUri(environment.getProperty(basePath 
                    + "authorization-uri"));
            clientRegistration.setUseRefreshTokens(Boolean.valueOf(environment.getProperty(
                    basePath + "use-refresh-tokens")));
            clientRegistration.setDefaultRedirectAfterCallback(environment.getProperty(basePath 
                    + "default-redirect-after-callback"));
            registrations.put(clientRegistration.getClientName(), clientRegistration);
        }
    }
}
