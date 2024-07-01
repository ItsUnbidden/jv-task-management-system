package com.unbidden.jvtaskmanagementsystem.service.oauth2;

import com.unbidden.jvtaskmanagementsystem.dto.oauth2.OAuth2SuccessResponse;
import com.unbidden.jvtaskmanagementsystem.exception.EntityNotFoundException;
import com.unbidden.jvtaskmanagementsystem.exception.oauth2.OAuth2AuthorizedClientLoadingException;
import com.unbidden.jvtaskmanagementsystem.model.ClientRegistration;
import com.unbidden.jvtaskmanagementsystem.model.OAuth2AuthorizedClient;
import com.unbidden.jvtaskmanagementsystem.model.User;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;

public interface OAuth2Service {
    /**
     * This method begins OAuth2 authorization code flow by forming uri
     * and redirecting to the authorization server.
     * If {@code origin} parameter is not {@code null} then after the authorization
     * is completed successfully, user will be redirected back to {@code origin}.
     * 
     * @param user that initiated the request
     * @param response for redirecting
     * @param clientRegistration contains required provider data
     * @param origin of the initial request if any
     */
    void authorize(@NonNull User user, @NonNull HttpServletResponse response,
            @NonNull ClientRegistration clientRegistration);

    /**
     * This method proccesses OAuth2 response from authorization server after
     * the user has granted or rejected required permissions.
     * @param response for redirecting
     * @param code returned by the server if permissions granted
     * @param state contains meta data
     * @param error if any
     * @param errorDescription if any
     */
    OAuth2SuccessResponse callback(@NonNull HttpServletResponse response, @NonNull String code,
            @NonNull String state, String error, String errorDescription);

    /**
     * Tries to load {@link OAuth2AuthorizedClient}. If an instance exists
     * but is expired, will try to use refresh token. If it does not exist or
     * refresh tokens are not available will throw {@link OAuth2AuthorizedClientLoadingException}.
     * @param user for which to load client
     * @param clientRegistration contains required provider data
     * @return instance of {@link OAuth2AuthorizedClient}
     * @throws OAuth2AuthorizedClientLoadingException if loading attempt failed
     */
    OAuth2AuthorizedClient loadAuthorizedClient(@NonNull User user,
            @NonNull ClientRegistration clientRegistration)
            throws OAuth2AuthorizedClientLoadingException;

    /**
     * Permanently deletes this {@link OAuth2AuthorizedClient}. This method should be called when
     * user manualy logs out of service or is removed. It should not be called when
     * token expires or in any other case.
     * @param authorizedClient which needs to be deleted
     */
    void deleteAuthorizedClient(@NonNull OAuth2AuthorizedClient authorizedClient);

    /**
     * Will fetch authorized client from the database or throw an {@link EntityNotFoundException}.
     * This method should not be used to load authorized clients for third party api operations,
     * use {@link #loadAuthorizedClient(User, ClientRegistration)} instead.
     * @param user for which to get client
     * @param clientRegistration contains required provider data
     * @return instance of {@link OAuth2AuthorizedClient}
     */
    OAuth2AuthorizedClient getAuthorizedClientForUser(@NonNull User user,
            @NonNull ClientRegistration clientRegistration);
}
