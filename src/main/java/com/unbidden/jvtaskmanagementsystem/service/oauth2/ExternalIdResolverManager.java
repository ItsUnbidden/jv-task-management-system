package com.unbidden.jvtaskmanagementsystem.service.oauth2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.unbidden.jvtaskmanagementsystem.model.ClientRegistration;
import com.unbidden.jvtaskmanagementsystem.service.oauth2.resolver.ExternalIdResolver;

@Component
public class ExternalIdResolverManager {
    private final Map<ClientRegistration, ExternalIdResolver> resolvers = new HashMap<>();

    public ExternalIdResolverManager(
            @Autowired List<ExternalIdResolver> resolverList) {
        for (ExternalIdResolver resolver : resolverList) {
            resolvers.put(resolver.getClientRegistration(), resolver);
        }
    }

    public ExternalIdResolver getResolver(ClientRegistration clientRegistration) {
        return resolvers.get(clientRegistration);
    }
}
