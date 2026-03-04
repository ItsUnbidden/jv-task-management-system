package com.unbidden.jvtaskmanagementsystem.service.oauth2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.unbidden.jvtaskmanagementsystem.model.ClientRegistration;
import com.unbidden.jvtaskmanagementsystem.service.oauth2.resolver.OAuth2AuthorizedClientResolver;

@Component
public class OAuth2AuthorizedClientResolverManager {
    private final Map<ClientRegistration, OAuth2AuthorizedClientResolver> resolvers = new HashMap<>();

    public OAuth2AuthorizedClientResolverManager(
            @Autowired List<OAuth2AuthorizedClientResolver> resolversList) {
        for (OAuth2AuthorizedClientResolver resolver : resolversList) {
            resolvers.put(resolver.getClientRegistration(), resolver);
        }
    }

    public OAuth2AuthorizedClientResolver getResolver(ClientRegistration clientRegistration) {
        return resolvers.get(clientRegistration);
    }
}
