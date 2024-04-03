package com.unbidden.jvtaskmanagementsystem.service.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unbidden.jvtaskmanagementsystem.dto.internal.dropbox.DropboxResponse;
import com.unbidden.jvtaskmanagementsystem.dto.internal.dropbox.error.GeneralError;
import com.unbidden.jvtaskmanagementsystem.dto.internal.dropbox.error.UploadError;
import com.unbidden.jvtaskmanagementsystem.dto.internal.dropbox.response.FileUploadResponse;
import com.unbidden.jvtaskmanagementsystem.dto.internal.dropbox.response.SimpleResponse;
import com.unbidden.jvtaskmanagementsystem.model.OAuth2AuthorizedClient;
import com.unbidden.jvtaskmanagementsystem.service.oauth2.OAuth2Service;
import com.unbidden.jvtaskmanagementsystem.service.util.HttpClientUtil;
import com.unbidden.jvtaskmanagementsystem.service.util.HttpClientUtil.HeaderNames;
import com.unbidden.jvtaskmanagementsystem.service.util.HttpClientUtil.HeaderValues;
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

    private static final String BASE_DROPBOX_API_CONTENT_URI = "https://content.dropboxapi.com/2";

    private static final String TMS_FOLDER_PATH = "/Task Management System";

    private final HttpClient httpClient;

    private final HttpClientUtil httpUtil;

    private final ObjectMapper objectMapper;

    private final OAuth2Service oauthService;

    public DropboxResponse test(OAuth2AuthorizedClient authorizedClient) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_DROPBOX_API_URI + "/check/user"))
                .POST(BodyPublishers.ofString("{\"query\": \"%s\"}".formatted(TEST_MESSAGE)))
                .setHeader(HeaderNames.AUTHORIZATION, httpUtil.getBearerAuthorizationHeader(
                    authorizedClient.getToken()))
                .setHeader(HeaderNames.CONTENT_TYPE, HeaderValues.APPLICATION_JSON)
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString()); 
            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), SimpleResponse.class);
            }
            return objectMapper.readValue(response.body(), GeneralError.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Unable to test dropbox client. Exception occured.", e);
        }
    }

    public DropboxResponse upload(OAuth2AuthorizedClient authorizedClient,
            String filename, byte[] data) {
        FileParameters parameters = new FileParameters(true, "add",
                false, "/" + filename, false);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(BodyPublishers.ofByteArray(data))
                    .uri(URI.create(BASE_DROPBOX_API_CONTENT_URI + "/files/upload"))
                    .setHeader(HttpClientUtil.HeaderNames.AUTHORIZATION, 
                        httpUtil.getBearerAuthorizationHeader(authorizedClient.getToken()))
                    .setHeader(HeaderNames.CONTENT_TYPE, HeaderValues.APPLICATION_OCTET_STREAM)
                    .header(HeaderNames.DROPBOX_API_ARG,
                        objectMapper.writeValueAsString(parameters))
                    .build();

            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), FileUploadResponse.class);
            }
            return objectMapper.readValue(response.body(), UploadError.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Unable to upload " + filename + " to dropbox.", e);
        }
    }

    public DropboxResponse logout(OAuth2AuthorizedClient authorizedClient) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_DROPBOX_API_URI + "/auth/token/revoke"))
                .POST(BodyPublishers.noBody())
                .setHeader(HeaderNames.AUTHORIZATION, httpUtil.getBearerAuthorizationHeader(
                    authorizedClient.getToken()))               
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                SimpleResponse result = new SimpleResponse();
                result.setResult("Logout successful. Token revoked.");
                oauthService.deleteAuthorizedClient(authorizedClient);
                return result;
            }
            return objectMapper.readValue(response.body(), GeneralError.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Unable to logout from dropbox client. "
                    + "Exception occured.", e);
        }
    }

    public static class FileParameters {
        @JsonProperty("autorename")
        private Boolean autorename;

        @JsonProperty("mode")
        private String mode;

        @JsonProperty("mute")
        private Boolean mute;

        @JsonProperty("path")
        private String path;

        @JsonProperty("strict_conflict")
        private Boolean strictConflict;

        public FileParameters(Boolean autorename, String mode, Boolean mute,
                String path, Boolean strictConflict) {
            this.autorename = autorename;
            this.mode = mode;
            this.mute = mute;
            this.path = path;
            this.strictConflict = strictConflict;
        }
    }
}
