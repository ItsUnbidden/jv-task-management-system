package com.unbidden.jvtaskmanagementsystem.service.oauth2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unbidden.jvtaskmanagementsystem.dto.internal.OAuth2TokenResponseDto;
import com.unbidden.jvtaskmanagementsystem.exception.OAuth2CodeExchangeException;
import com.unbidden.jvtaskmanagementsystem.model.ClientRegistration;
import com.unbidden.jvtaskmanagementsystem.model.OAuth2AuthorizedClient;
import com.unbidden.jvtaskmanagementsystem.service.util.HttpClientUtil;
import com.unbidden.jvtaskmanagementsystem.service.util.HttpClientUtil.HeaderNames;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@SuppressWarnings("null")
public class OAuth2Client {
    private static final String AUTHORIZATION_CODE_TOKEN_URI_BASE = 
            "%s?code=%s&redirect_uri=%s&grant_type=authorization_code";

    private static final String REFRESH_TOKEN_URI_BASE = 
            "%s?grant_type=refresh_token&refresh_token=%s";

    private final HttpClient http;

    private final ObjectMapper objectMapper;

    private final HttpClientUtil httpUtil;

    @NonNull
    public OAuth2TokenResponseDto exchange(@NonNull String code, 
            @NonNull ClientRegistration clientRegistration) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AUTHORIZATION_CODE_TOKEN_URI_BASE.formatted(
                    clientRegistration.getTokenUri(), code, clientRegistration.getRedirectUri())))
                .POST(BodyPublishers.noBody())
                .setHeader(HeaderNames.AUTHORIZATION,
                    httpUtil.getBasicAuthorizationHeader(clientRegistration.getClientId(),
                    clientRegistration.getClientSecret()))
                .build();
        
        return executeExchange(request);
    }

    @NonNull
    public OAuth2TokenResponseDto refresh(OAuth2AuthorizedClient authorizedClient,
            ClientRegistration clientRegistration) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(REFRESH_TOKEN_URI_BASE.formatted(clientRegistration.getTokenUri(),
                    authorizedClient.getRefreshToken())))
                .POST(BodyPublishers.noBody())
                .setHeader(HeaderNames.AUTHORIZATION,
                    httpUtil.getBasicAuthorizationHeader(clientRegistration.getClientId(),
                    clientRegistration.getClientSecret()))
                .build();

        return executeExchange(request);
    }

    private OAuth2TokenResponseDto executeExchange(HttpRequest request) {
        HttpResponse<String> response = null;
        try {
            response = http.send(request, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new OAuth2CodeExchangeException("Client was unable to send " 
                    + "a request to authorization server.", e);
        } 
    
        if (response.statusCode() != 200) {
            throw new OAuth2CodeExchangeException("Server was not able to respond correctly. " 
                    + "Response: " + response.body());
        }

        try {
            return objectMapper.readValue(response.body(), OAuth2TokenResponseDto.class);
        } catch (JsonProcessingException e) {
            throw new OAuth2CodeExchangeException("Client was unable to parse authorization " 
                    + "server response during code exchange.", e);
        }
    }
}
