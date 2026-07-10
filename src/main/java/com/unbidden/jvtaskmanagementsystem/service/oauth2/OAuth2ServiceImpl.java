package com.unbidden.jvtaskmanagementsystem.service.oauth2;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.unbidden.jvtaskmanagementsystem.dto.oauth2.OAuth2StatusResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.oauth2.OAuth2StatusResponseDto.OAuth2Status;
import com.unbidden.jvtaskmanagementsystem.dto.oauth2.OAuth2TokenResponseDto;
import com.unbidden.jvtaskmanagementsystem.exception.EntityNotFoundException;
import com.unbidden.jvtaskmanagementsystem.exception.ErrorType;
import com.unbidden.jvtaskmanagementsystem.exception.StateCollisionException;
import com.unbidden.jvtaskmanagementsystem.exception.oauth2.OAuth2AuthorizationException;
import com.unbidden.jvtaskmanagementsystem.exception.oauth2.OAuth2AuthorizedClientLoadingException;
import com.unbidden.jvtaskmanagementsystem.exception.oauth2.OAuth2CodeExchangeException;
import com.unbidden.jvtaskmanagementsystem.exception.oauth2.OAuth2ExternalIdException;
import com.unbidden.jvtaskmanagementsystem.model.AuthorizationMeta;
import com.unbidden.jvtaskmanagementsystem.model.ClientRegistration;
import com.unbidden.jvtaskmanagementsystem.model.OAuth2AuthorizedClient;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.repository.oauth2.AuthorizationMetaRepository;
import com.unbidden.jvtaskmanagementsystem.service.oauth2.resolver.ExternalIdResolver;
import com.unbidden.jvtaskmanagementsystem.util.HttpClientUtil.HeaderNames;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OAuth2ServiceImpl implements OAuth2Service {
	private static final Logger LOGGER = LogManager.getLogger(OAuth2ServiceImpl.class);

	private static final String DEFAULT_RETURN_URL = "/dashboard";

	private static final String AUTHORIZATION_URI_PATTERN = 
			"%s?client_id=%s&redirect_uri=%s&state=%s"
			+ "&token_access_type=%s&access_type=%s&scope=%s&response_type=code&prompt=consent";

	private final ExternalIdResolverManager externalIdResolverManager;

	private final OAuth2AuthorizedClientService authClientService;

	private final AuthorizationMetaRepository authorizationMetaRepository;

	private final OAuth2Client oauthClient;

	@Value("${frontend.base-url}")
	private String frontendBaseUrl;

	@Override
	public void authorize(@NonNull User user, @NonNull HttpServletResponse response,
			@NonNull ClientRegistration clientRegistration, @NonNull String returnUrl) {
		try {
			loadAuthorizedClient(user, clientRegistration);
			throw new StateCollisionException("No need to continue authorization"
					+ " because user " + user.getId() + " is already authorized by "
					+ clientRegistration.getClientName() + ".",
					ErrorType.OAUTH2_ALREADY_AUTHORIZED);
		} catch (OAuth2AuthorizedClientLoadingException e) {
			LOGGER.debug("User " + user.getId() + " is not authorized. "
					+ "Beginning authorization flow.");
		}
		final AuthorizationMeta meta = getAuthorizationMeta(user, clientRegistration, validateReturnUrl(returnUrl));
		LOGGER.debug("Authorization meta formed for user " + user.getId());
		
		final String authUri = AUTHORIZATION_URI_PATTERN.formatted(
				clientRegistration.getAuthorizationUri(),
				clientRegistration.getClientId(),
				clientRegistration.getRedirectUri(),
				meta.getId().toString(),
				(clientRegistration.getUseRefreshTokens()) ? "offline" : "online",
				(clientRegistration.getUseRefreshTokens()) ? "offline" : "online",
				clientRegistration.getScope());
		LOGGER.debug("Authorization URL is formed for user " + user.getId());
		try {
			response.setStatus(302);
			response.setHeader(HeaderNames.LOCATION, authUri);
			response.flushBuffer();
			LOGGER.debug("Response is configured for authorization redirect for user "
					+ user.getId());
		} catch (IOException e) {
			throw new OAuth2AuthorizationException("An IOException occured during "
					+ "redirect commit.", ErrorType.OAUTH2_INTERNAL_FAILURE, e);
		}
	}
	
	/**
	 * {@code result} can only have two values: {@code success} and {@code error}.
	 * If {@code result} is {@code success}, then {@code reason} will always be missing.
	 * On the other hand, if it's {@code error}, {@code reason} will always be one of these values:
	 * <ls>
	 * 	<li>{@code external} — a third-party issue has occured or the user has declined to authorize the app.</li>
	 * 	<li>{@code taken} — this external ID has already been taken by someone else.</li>
	 * 	<li>{@code exchange} — token exchange has failed.</li>
	 * 	<li>{@code id} — failed to fetch the external ID.</li>
	 * </ls>
	 */
	@Override
	public void callback(@NonNull HttpServletResponse response,
			String code, @NonNull String state, String error, String errorDescription) {
		LOGGER.debug("Callback was called. State: " + state);
		final AuthorizationMeta meta = authorizationMetaRepository.load(state)
				.orElseThrow(() -> new EntityNotFoundException("There is no authorization meta "
				+ "associated with provided state: " + state + ". This may indicate that the "
				+ "authorization attempt took too long.", ErrorType.OAUTH2_NO_STATE_FOUND));
		LOGGER.debug("Meta loaded. User id: " + meta.getUser().getId());

		String result;
		String reason;
		if (error != null && !error.isBlank()) {
			LOGGER.warn("An error has occured while authorizing "
					+ meta.getClientRegistration().getClientName() + " for user "
					+ meta.getUser().getUsername() + ". Error: " + error
					+ ". Description: " + errorDescription + ".");
			result = "error";
			reason = "external";
		} else {
			try {
				final OAuth2TokenResponseDto tokenData = oauthClient.exchange(code,
						meta.getClientRegistration());

				LOGGER.debug("Code exchanged for token for user " + meta.getId());
				final ExternalIdResolver idResolver = externalIdResolverManager.getResolver(
						meta.getClientRegistration());
				final String externalId = idResolver.resolveId(meta.getUser(),
						tokenData.getAccessToken());

				if (authClientService.isExternalIdTaken(externalId)) {
					LOGGER.debug("User " + meta.getUser().getUsername() + " cannot use external "
							+ "ID of the authorized account, because that account is already "
							+ "in use by someone else. The new token will be revoked.");
					
					try {
						idResolver.revokeToken(meta.getUser(), tokenData.getAccessToken());
					} catch (OAuth2ExternalIdException e) {
						LOGGER.warn("Failed to revoke the newly assigned "
								+ meta.getClientRegistration().getClientName()
								+ " token for user " + meta.getUser().getUsername() + ".", e);
					}
					result = "error";
					reason = "taken";
				} else {
					authClientService.register(meta.getUser(), tokenData, meta.getClientRegistration(),
							externalId);
					LOGGER.debug("Authorization flow for user " + meta.getUser().getId()
							+ " has been completed successfuly.");
					result = "success";
					reason = null;
				}
			} catch (OAuth2CodeExchangeException e) {
				LOGGER.warn("An error has occured while exchanging the token for "
						+ meta.getClientRegistration().getClientName() + " for user "
						+ meta.getUser().getUsername() + ".");
				result = "error";
				reason = "exchange";
			} catch (OAuth2ExternalIdException e) {
				LOGGER.warn("An error has occured while fetching external ID for "
						+ meta.getClientRegistration().getClientName() + " for user "
						+ meta.getUser().getUsername() + ".");
				result = "error";
				reason = "id";
			}
		}
		String redirectUrl = frontendBaseUrl + meta.getReturnUrl()
				+ "?oauth=%s&result=%s".formatted(
				meta.getClientRegistration().getClientName(), result);

		if (reason != null) {
			redirectUrl += "&reason=%s".formatted(reason);
		}

		try {
			response.setStatus(302);
			response.setHeader(HeaderNames.LOCATION, redirectUrl);
			response.flushBuffer();
		} catch (IOException e) {
			LOGGER.error("Failed to flush buffer for HTTP response.", e);
		}
	}

	@Override
	public OAuth2AuthorizedClient loadAuthorizedClient(User user,
			ClientRegistration clientRegistration) throws OAuth2AuthorizedClientLoadingException {
		LOGGER.debug("Attempting to load authorized client for user " + user.getId()
				+ " and client registration " + clientRegistration.getClientName());
		final Optional<OAuth2AuthorizedClient> authorizedClientOpt = authClientService.findClient(
				user, clientRegistration);

		if (authorizedClientOpt.isPresent()) {
			OAuth2AuthorizedClient authorizedClient = authorizedClientOpt.get();
			LOGGER.debug("Authorized client found.");

			if (authorizedClient.getAquiredAt()
					.plusSeconds(authorizedClient.getExpiresIn())
					.isAfter(LocalDateTime.now())) {
				LOGGER.debug("Authorized client is valid.");
				return authorizedClient;
			}
			if (clientRegistration.getUseRefreshTokens()) {
				LOGGER.debug("Authorized client is expired. "
						+ "Refresh tokens are enabled. Attempting to refresh.");
				try {
					final OAuth2TokenResponseDto tokenData = oauthClient.refresh(authorizedClient,
							clientRegistration);
					
					return authClientService.update(authorizedClient.getId(), tokenData);
				} catch (OAuth2CodeExchangeException e) {
					LOGGER.debug("Unable to complete token refresh. Authorized client for user "
							+ user.getId() + " and service " + clientRegistration.getClientName()
							+ " is now considered obsolete and will be deleted.", e);
					deleteAuthorizedClient(authorizedClient.getId());
					LOGGER.debug("Authorized client for user " + user.getId() + " and service "
							+ clientRegistration.getClientName() + " has been deleted.");
					throw new OAuth2AuthorizedClientLoadingException("Unable to load authorized "
							+ "client for user " + user.getUsername()
							+ " because the refresh flow has failed.", e);
				} catch (EntityNotFoundException e) {
					LOGGER.warn("Failed to find authorized client " + authorizedClient.getId()
							+ ". It might be a state inconsistency issue.");
					throw new OAuth2AuthorizedClientLoadingException("Unable to load authorized "
							+ "client " + authorizedClient.getId()
							+ ", because it does not exist.", e);
				}
			} else {
				throw new OAuth2AuthorizedClientLoadingException("Unable to load authorized "
						+ "client for user " + user.getUsername()
						+ " because it has expired. Refresh tokens are unavailable to "
						+ "attempt to update it.");
			}   
		}
		throw new OAuth2AuthorizedClientLoadingException("Unable to load authorized "
				+ "client for user " + user.getUsername() + " because it does not exist.");
	}

	@Override
	public void deleteAuthorizedClient(@NonNull Long id) {
		authClientService.delete(id);
	}

	@Override
	public OAuth2AuthorizedClient getAuthorizedClientForUser(User user,
			ClientRegistration clientRegistration) {
		return authClientService.findClient(user, clientRegistration).orElseThrow(
				() -> new EntityNotFoundException(
				"Unable to find an authorized client with user " + user.getId()
				+ " and client registration " + clientRegistration.getClientName(),
				ErrorType.OAUTH2_INTERNAL_FAILURE));
	}

	@Override
	public OAuth2StatusResponseDto checkStatus(User user, ClientRegistration clientRegistration) {
		final Optional<OAuth2AuthorizedClient> authorizedClientOpt = authClientService
				.findClient(user, clientRegistration);
		
		if (authorizedClientOpt.isPresent()) {
			final OAuth2AuthorizedClient client = authorizedClientOpt.get();

			return (client.getAquiredAt().plusSeconds(client.getExpiresIn()).isAfter(LocalDateTime.now()))
					? new OAuth2StatusResponseDto(OAuth2Status.OK, client.getAquiredAt())
					: new OAuth2StatusResponseDto(OAuth2Status.EXPIRED, client.getAquiredAt());
		}
		return new OAuth2StatusResponseDto(OAuth2Status.NOT_CONNECTED, null);
	}

	private AuthorizationMeta getAuthorizationMeta(User user,
			ClientRegistration clientRegistration, String returnUrl) {
		final UUID uuid = UUID.randomUUID();

		return authorizationMetaRepository.save(new AuthorizationMeta(uuid, user,
				clientRegistration, LocalDateTime.now(), returnUrl));
	}

	private String validateReturnUrl(String returnUrl) {
		if (!returnUrl.startsWith("//") && returnUrl.startsWith("/")) {
			return returnUrl;
		}
		LOGGER.info("Return URL \"" + returnUrl + "\" is invalid. Using default return URL.");
		return DEFAULT_RETURN_URL;
	}
}
