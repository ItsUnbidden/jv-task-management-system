package com.unbidden.jvtaskmanagementsystem.service.oauth2;

import com.unbidden.jvtaskmanagementsystem.dto.oauth2.OAuth2SuccessResponse;
import com.unbidden.jvtaskmanagementsystem.dto.oauth2.OAuth2TokenResponseDto;
import com.unbidden.jvtaskmanagementsystem.exception.EntityNotFoundException;
import com.unbidden.jvtaskmanagementsystem.exception.oauth2.OAuth2AuthorizationException;
import com.unbidden.jvtaskmanagementsystem.exception.oauth2.OAuth2AuthorizedClientLoadingException;
import com.unbidden.jvtaskmanagementsystem.exception.oauth2.OAuth2CodeExchangeException;
import com.unbidden.jvtaskmanagementsystem.model.AuthorizationMeta;
import com.unbidden.jvtaskmanagementsystem.model.ClientRegistration;
import com.unbidden.jvtaskmanagementsystem.model.OAuth2AuthorizedClient;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.repository.oauth2.AuthorizationMetaRepository;
import com.unbidden.jvtaskmanagementsystem.repository.oauth2.AuthorizedClientRepository;
import com.unbidden.jvtaskmanagementsystem.util.HttpClientUtil.HeaderNames;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuth2ServiceImpl implements OAuth2Service {
    private static final Logger LOGGER = LogManager.getLogger(OAuth2ServiceImpl.class);

    private static final String AUTHORIZATION_URI_PATTERN = 
            "%s?client_id=%s&redirect_uri=%s&state=%s"
            + "&token_access_type=%s&access_type=%s&scope=%s&response_type=code&prompt=consent";

    private final OAuth2AuthorizedClientResolverManager authorizedClientResolverManager;
    
    private final AuthorizationMetaRepository authorizationMetaRepository;

    private final OAuth2Client oauthClient;

    private final AuthorizedClientRepository authorizedClientRepository;

    @Override
    public void authorize(@NonNull User user, @NonNull HttpServletResponse response,
            @NonNull ClientRegistration clientRegistration) {
        try {
            loadAuthorizedClient(user, clientRegistration);
            throw new OAuth2AuthorizationException("No need to continue authorization"
                    + " because user " + user.getId() + " is already authorized by "
                    + clientRegistration.getClientName() + ".");
        } catch (OAuth2AuthorizedClientLoadingException e) {
            LOGGER.info("User " + user.getId() + " is not authorized. "
                    + "Beginning authorization flow.");
        }
        final AuthorizationMeta meta = getAuthorizationMeta(user, clientRegistration);
        LOGGER.info("Authorization meta formed for user " + user.getId());
        
        final String authUri = AUTHORIZATION_URI_PATTERN.formatted(
                clientRegistration.getAuthorizationUri(),
                clientRegistration.getClientId(),
                clientRegistration.getRedirectUri(),
                meta.getId().toString(),
                (clientRegistration.getUseRefreshTokens()) ? "offline" : "online",
                (clientRegistration.getUseRefreshTokens()) ? "offline" : "online",
                clientRegistration.getScope());
        LOGGER.info("Authorization URL is formed for user " + user.getId());
        try {
            response.setStatus(302);
            response.addHeader(HeaderNames.LOCATION, authUri);
            response.flushBuffer();
            LOGGER.info("Response is configured for authorization redirect for user "
                    + user.getId());
        } catch (IOException e) {
            throw new OAuth2AuthorizationException("IOException occured during "
                    + "redirect commit.", e);
        }
    }
    
    @Override
    public OAuth2SuccessResponse callback(@NonNull HttpServletResponse response,
            @NonNull String code, @NonNull String state, String error, String errorDescription) {
        LOGGER.info("Callback was called. State: " + state);
        AuthorizationMeta meta = authorizationMetaRepository.load(state)
                .orElseThrow(() -> new EntityNotFoundException("There is no authorization meta "
                + "associated with provided state: <" + state + ">. This may indicate that the "
                + "authorization attempt took too long."));
        LOGGER.info("Meta loaded. User id: " + meta.getUser().getId());
        if (error != null && !error.isBlank()) {
            throw new OAuth2AuthorizationException("Error has occured during " 
                    + meta.getClientRegistration().getClientName() 
                    + " authorization. Error: " + error + "; description: " + errorDescription);
        }

        try {
            OAuth2TokenResponseDto tokenData = oauthClient.exchange(code,
                    meta.getClientRegistration());
            LOGGER.info("Code exchanged for token for user " + meta.getId());
            Optional<OAuth2AuthorizedClient> authorizedClientOpt = authorizedClientRepository
                    .findByUserIdAndRegistrationName(meta.getUser().getId(),
                    meta.getClientRegistration().getClientName());
            authorizedClientResolverManager.getResolver(meta.getClientRegistration())
                    .resolveAuthorizedClient(meta.getUser(), tokenData, authorizedClientOpt);
            LOGGER.info("Authorization flow for user " + meta.getUser().getId()
                    + " has been completed successfuly.");
            return new OAuth2SuccessResponse("OAuth2 Authorization Flow has been "
                    + "concluded. Service " + meta.getClientRegistration().getClientName()
                    + " has been connected successfuly for user " + meta.getUser().getId() + ".");
        } catch (OAuth2CodeExchangeException e) {
            throw new OAuth2AuthorizationException("Unable to recieve token data and complete "
                    + "authorization into service "
                    + meta.getClientRegistration().getClientName(), e);
        }
    }

    @Override
    public OAuth2AuthorizedClient loadAuthorizedClient(User user,
            ClientRegistration clientRegistration) throws OAuth2AuthorizedClientLoadingException {
        LOGGER.info("Attempting to load authorized client for user " + user.getId()
                + " and client registration " + clientRegistration.getClientName());
        Optional<OAuth2AuthorizedClient> authorizedClientOpt = authorizedClientRepository
                .findByUserIdAndRegistrationName(user.getId(),clientRegistration.getClientName());
        if (authorizedClientOpt.isPresent()) {
            OAuth2AuthorizedClient authorizedClient = authorizedClientOpt.get();
            LOGGER.info("Authorized client found.");

            if (authorizedClient.getAquiredAt()
                    .plusSeconds(authorizedClient.getExpiresIn())
                    .isAfter(LocalDateTime.now())) {
                LOGGER.info("Authorized client is valid.");
                return authorizedClient;
            }
            if (clientRegistration.getUseRefreshTokens()) {
                LOGGER.info("Authorized client is invalid. "
                        + "Refresh tokens are enabled. Attempting to refresh.");
                try {
                    return authorizedClientResolverManager.getResolver(clientRegistration)
                        .resolveAuthorizedClient(user, oauthClient.refresh(authorizedClient,
                        clientRegistration), authorizedClientOpt);
                } catch (OAuth2CodeExchangeException e) {
                    LOGGER.error("Unable to complete token refresh. Authorized client for user "
                            + user.getId() + " and service " + clientRegistration.getClientName()
                            + " is now considered obsolete and will be deleted.", e);
                    deleteAuthorizedClient(authorizedClient);
                    LOGGER.info("Authorized client for user " + user.getId() + " and service "
                            + clientRegistration.getClientName() + " has been deleted.");
                }
            }     
        }
        throw new OAuth2AuthorizedClientLoadingException("Unable to load authorized client "
                + "for user " + user.getId() + ". "
                + "It is either expired and refresh tokens are not available, "
                + "access was revoked and refresh failed "
                + "or the user has never been logged in.");
    }

    @Override
    public void deleteAuthorizedClient(OAuth2AuthorizedClient authorizedClient) {
        authorizedClientRepository.delete(authorizedClient);
    }

    @Override
    public OAuth2AuthorizedClient getAuthorizedClientForUser(User user,
            ClientRegistration clientRegistration) {
        return authorizedClientRepository.findByUserIdAndRegistrationName(user.getId(),
                clientRegistration.getClientName()).orElseThrow(() -> new EntityNotFoundException(
                "Unable to find an authorized client with user " + user.getId()
                + " and client registration " + clientRegistration.getClientName()));
    }

    private AuthorizationMeta getAuthorizationMeta(User user,
            ClientRegistration clientRegistration) {
        final UUID uuid = UUID.randomUUID();

        return authorizationMetaRepository.save(new AuthorizationMeta(uuid, user,
                clientRegistration, LocalDateTime.now()));
    }
}
