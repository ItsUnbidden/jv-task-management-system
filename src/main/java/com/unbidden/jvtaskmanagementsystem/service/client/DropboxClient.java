package com.unbidden.jvtaskmanagementsystem.service.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unbidden.jvtaskmanagementsystem.dto.internal.DropboxTestResponseDto;
import com.unbidden.jvtaskmanagementsystem.model.OAuth2AuthorizedClient;
import com.unbidden.jvtaskmanagementsystem.service.util.HttpClientUtil;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DropboxClient {
    private static final String TEST_MESSAGE = "If you see this message, " 
            + "it means that dropbox is connected correctly.";
            
    private static final String BASE_DROPBOX_API_URI = "https://api.dropboxapi.com/2";

    private final HttpClient httpClient;

    private final HttpClientUtil httpUtil;

    private final ObjectMapper objectMapper;

    public DropboxTestResponseDto test(OAuth2AuthorizedClient authorizedClient) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_DROPBOX_API_URI + "/check/user"))
                .POST(BodyPublishers.ofString("{\"query\": \"%s\"}".formatted(TEST_MESSAGE)))
                .setHeader(HttpClientUtil.AUTHORIZATION, httpUtil.getBearerAuthorizationHeader(
                    authorizedClient.getToken()))
                .setHeader(HttpClientUtil.CONTENT_TYPE, HttpClientUtil.APPLICATION_JSON)
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            DropboxTestResponseDto result = 
                    objectMapper.readValue(response.body(), DropboxTestResponseDto.class);
            return result;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Unable to test dropbox client. Exception occured.", e);
        }
    }
}
