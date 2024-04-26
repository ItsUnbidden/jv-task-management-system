package com.unbidden.jvtaskmanagementsystem.service.oauth2.resolver;

import com.unbidden.jvtaskmanagementsystem.dto.internal.OAuth2TokenResponseDto;
import com.unbidden.jvtaskmanagementsystem.model.ClientRegistration;
import com.unbidden.jvtaskmanagementsystem.model.OAuth2AuthorizedClient;
import com.unbidden.jvtaskmanagementsystem.model.User;
import java.util.Optional;

public interface OAuth2AuthorizedClientResolver {
    OAuth2AuthorizedClient resolveAuthorizedClient(User user, OAuth2TokenResponseDto tokenData,
            Optional<OAuth2AuthorizedClient> authorizedClientOpt);

    ClientRegistration getClientRegistration();
}
