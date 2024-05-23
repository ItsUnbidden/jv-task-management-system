package com.unbidden.jvtaskmanagementsystem.service.oauth2.resolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unbidden.jvtaskmanagementsystem.dto.oauth2.GoogleUserInfoResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.oauth2.OAuth2TokenResponseDto;
import com.unbidden.jvtaskmanagementsystem.exception.ThirdPartyApiException;
import com.unbidden.jvtaskmanagementsystem.exception.oauth2.OAuth2AuthorizationException;
import com.unbidden.jvtaskmanagementsystem.model.OAuth2AuthorizedClient;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.repository.oauth2.AuthorizedClientRepository;
import com.unbidden.jvtaskmanagementsystem.util.EntityUtil;
import com.unbidden.jvtaskmanagementsystem.util.HttpClientUtil;
import com.unbidden.jvtaskmanagementsystem.util.HttpClientUtil.HeaderNames;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OAuth2AuthorizedClientGoogleResolver extends OAuth2AuthorizedClientAbstractResolver {
    private static final Logger LOGGER =
            LogManager.getLogger(OAuth2AuthorizedClientGoogleResolver.class);

    private static final String GOOGLE_USER_INFO_URL =
            "https://www.googleapis.com/oauth2/v1/userinfo";

    private final HttpClient http;

    private final ObjectMapper objectMapper;

    private final HttpClientUtil httpUtil;

    public OAuth2AuthorizedClientGoogleResolver(@Autowired EntityUtil entityUtil,
            @Autowired AuthorizedClientRepository authorizedClientRepository,
            @Autowired HttpClient http,
            @Autowired ObjectMapper objectMapper,
            @Autowired HttpClientUtil httpUtil) {
        super(entityUtil.getClientRegistrationByName("google"), authorizedClientRepository);
        this.http = http;
        this.objectMapper = objectMapper;
        this.httpUtil = httpUtil;
    }

    @Override
    public OAuth2AuthorizedClient resolveAuthorizedClient(User user,
            OAuth2TokenResponseDto tokenData,
            Optional<OAuth2AuthorizedClient> authorizedClientOpt) {
        LOGGER.info("Calling super method for user " + user.getId());
        OAuth2AuthorizedClient authorizedClient =
                super.resolveAuthorizedClient(user, tokenData, authorizedClientOpt);
        authorizedClient.setExternalAccountId(
                getEmailFromGoogle(authorizedClient));
        Optional<OAuth2AuthorizedClient> authClientWithCurrentExtIdOpt =
                authorizedClientRepository.findByExternalAccountId(authorizedClient
                .getExternalAccountId());
        LOGGER.info("Testing whether the external id is taken.");
        if (authClientWithCurrentExtIdOpt.isPresent()
                && authClientWithCurrentExtIdOpt.get().getUser().getId() != user.getId()) {
            authorizedClientRepository.deleteById(authorizedClient.getId());
            throw new OAuth2AuthorizationException("External account id "
                    + authorizedClient.getExternalAccountId()
                    + " is already taken by another user. While that user is authorized, no"
                    + " other user is allowed to authorize with this google account.");
        }
        LOGGER.info("Persisting updated authorized client to db for user " + user.getId());
        return authorizedClientRepository.save(authorizedClient);
    }

    private String getEmailFromGoogle(OAuth2AuthorizedClient authorizedClient) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GOOGLE_USER_INFO_URL))
                .GET()
                .setHeader(HeaderNames.AUTHORIZATION,
                    httpUtil.getBearerAuthorizationHeader(authorizedClient.getToken()))
                .build();
        LOGGER.info("Sending request for user " + authorizedClient.getUser().getId()
                + "'s profile info...");
        try {
            HttpResponse<String> response = http.send(request, BodyHandlers.ofString());
            LOGGER.info("Response received.");

            GoogleUserInfoResponseDto result =
                    objectMapper.readValue(response.body(), GoogleUserInfoResponseDto.class);
            LOGGER.info("Response parsed.");
            return result.getEmail();
        } catch (IOException | InterruptedException e) {
            throw new ThirdPartyApiException("Unable to send request for user "
                    + authorizedClient.getUser().getId()
                    + "'s profile info from google or unable to process response.", e);
        }
    }
}
