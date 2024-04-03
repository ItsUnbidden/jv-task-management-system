package com.unbidden.jvtaskmanagementsystem.service.oauth2;

import com.unbidden.jvtaskmanagementsystem.dto.internal.OAuth2TokenResponseDto;
import com.unbidden.jvtaskmanagementsystem.exception.EntityNotFoundException;
import com.unbidden.jvtaskmanagementsystem.exception.OAuth2AuthorizationException;
import com.unbidden.jvtaskmanagementsystem.model.AuthorizationMeta;
import com.unbidden.jvtaskmanagementsystem.model.ClientRegistration;
import com.unbidden.jvtaskmanagementsystem.model.OAuth2AuthorizedClient;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.repository.oauth2.AuthorizationMetaRepository;
import com.unbidden.jvtaskmanagementsystem.repository.oauth2.AuthorizedClientRepository;
import com.unbidden.jvtaskmanagementsystem.service.util.HttpClientUtil;
import com.unbidden.jvtaskmanagementsystem.service.util.HttpClientUtil.HeaderNames;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class OAuth2ServiceImpl implements OAuth2Service {
    private static final String AUTHORIZATION_URI_PATTERN = 
            "%s?client_id=%s&redirect_uri=%s&state=%s"
            + "&token_access_type=%s&response_type=code";

    private static final String CONNECT_ENDPOINT_BASE = "%s/oauth2/connect/%s?origin=%s";
    
    private final AuthorizationMetaRepository authorizationMetaRepository;

    private final OAuth2Client oauthClient;

    private final AuthorizedClientRepository authorizedClientRepository;

    private final HttpClientUtil httpUtil;

    @Override
    public void authorize(@NonNull User user, @NonNull HttpServletResponse response,
            @NonNull ClientRegistration clientRegistration, String origin) {
        final UUID uuid = UUID.randomUUID();
        final AuthorizationMeta authorizationMeta = 
                new AuthorizationMeta(uuid, user, clientRegistration, 
                LocalDateTime.now(), origin);

        authorizationMetaRepository.save(authorizationMeta);
        final String authUri = AUTHORIZATION_URI_PATTERN.formatted(
                clientRegistration.getAuthorizationUri(),
                clientRegistration.getClientId(),
                clientRegistration.getRedirectUri(),
                uuid.toString(),
                (clientRegistration.getUseRefreshTokens()) ? "offline" : "online");
        response.setHeader(HeaderNames.LOCATION, authUri);
        response.setStatus(302);
    }
    
    @Override
    public void callback(@NonNull HttpServletResponse response, @NonNull String code,
            @NonNull String state, String error, String errorDescription) {
        final String root = ServletUriComponentsBuilder.fromCurrentContextPath()
                .build()
                .toUriString();

        AuthorizationMeta meta = authorizationMetaRepository.load(state)
                .orElseThrow(() -> new EntityNotFoundException("There is no authorization meta "
                + "associated with provided state: <" + state + ">. This may indicate that the "
                + "authorization attempt took too long."));

        if (error != null && !error.isBlank()) {
            throw new OAuth2AuthorizationException("Error has occured during " 
                    + meta.getClientRegistration().getClientName() 
                    + " authorization. Error: " + error + "; description: " + errorDescription);
        }

        resolveAuthorizedClient(meta.getClientRegistration(),
                meta.getUser(),
                oauthClient.exchange(code, meta.getClientRegistration()),
                authorizedClientRepository.findByUserIdAndRegistrationName(meta.getUser().getId(),
                    meta.getClientRegistration().getClientName()).orElse(null));

        String redirectTo = (meta.getOrigin() == null || meta.getOrigin().isBlank()) 
                ? root + meta.getClientRegistration().getDefaultRedirectAfterCallback()
                : root + meta.getOrigin();
        response.setHeader(HeaderNames.LOCATION, redirectTo);
        response.setStatus(302);
    }

    @Override
    public OAuth2AuthorizedClient loadAuthorizedClient(@NonNull Authentication authentication,
            @NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull ClientRegistration clientRegistration) {
        final String root = ServletUriComponentsBuilder.fromCurrentContextPath()
                .build()
                .toUriString();
        final User user = (User)authentication.getPrincipal();

        Optional<OAuth2AuthorizedClient> optional = authorizedClientRepository
                .findByUserIdAndRegistrationName(user.getId(),
                clientRegistration.getClientName());
        if (optional.isPresent()) {
            OAuth2AuthorizedClient authorizedClient = optional.get();

            if (authorizedClient.getAquiredAt()
                    .plusSeconds(authorizedClient.getExpiresIn())
                    .isAfter(LocalDateTime.now())) {
                return authorizedClient;
            }  
            if (clientRegistration.getUseRefreshTokens()) {
                resolveAuthorizedClient(clientRegistration,
                        user, 
                        oauthClient.refresh(authorizedClient, clientRegistration),
                        authorizedClient);
                return authorizedClient;
            }         
        }
        response.setHeader(HeaderNames.LOCATION, CONNECT_ENDPOINT_BASE.formatted(
                root, clientRegistration.getClientName(), request.getRequestURI()));
        response.setStatus(302);
        response.setHeader(HeaderNames.AUTHORIZATION,
                httpUtil.getBearerAuthorizationHeader((String)authentication.getCredentials()));
        return null;
    }

    private void resolveAuthorizedClient(ClientRegistration clientRegistration,
            User user,
            OAuth2TokenResponseDto tokenData,
            OAuth2AuthorizedClient authorizedClient) {

        if (authorizedClient == null) {
            authorizedClient = new OAuth2AuthorizedClient();
        }

        authorizedClient.setAquiredAt(LocalDateTime.now());
        authorizedClient.setClientRegistrationName(clientRegistration.getClientName());
        authorizedClient.setUser(user);

        authorizedClient.setToken(tokenData.getAccessToken());
        authorizedClient.setExpiresIn(tokenData.getExpiresIn());
        
        if (tokenData.getRefreshToken() != null) {
            authorizedClient.setRefreshToken(tokenData.getRefreshToken());
        }

        authorizedClientRepository.save(authorizedClient);
    }

    @Override
    public void deleteAuthorizedClient(OAuth2AuthorizedClient authorizedClient) {
        authorizedClientRepository.delete(authorizedClient);
    }
}
