package com.unbidden.jvtaskmanagementsystem.service.oauth2.resolver;

import org.springframework.lang.NonNull;

import com.unbidden.jvtaskmanagementsystem.exception.oauth2.OAuth2ExternalIdException;
import com.unbidden.jvtaskmanagementsystem.model.ClientRegistration;
import com.unbidden.jvtaskmanagementsystem.model.User;

public interface ExternalIdResolver {
    String resolveId(@NonNull User user, @NonNull String token) throws OAuth2ExternalIdException;

    void revokeToken(@NonNull User user, @NonNull String token) throws OAuth2ExternalIdException;

    ClientRegistration getClientRegistration();
}
