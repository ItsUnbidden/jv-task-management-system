package com.unbidden.jvtaskmanagementsystem.service.oauth2;

import com.unbidden.jvtaskmanagementsystem.model.ClientRegistration;
import com.unbidden.jvtaskmanagementsystem.model.OAuth2AuthorizedClient;
import com.unbidden.jvtaskmanagementsystem.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;

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
            @NonNull ClientRegistration clientRegistration,
            String origin);

    /**
     * This method proccesses OAuth2 response from authorization server after
     * the user has granted of rejected required permissions.
     * @param response for redirecting
     * @param code returned by the server if permissions granted
     * @param state contains meta data
     * @param error if any
     * @param errorDescription if any
     */
    void callback(@NonNull HttpServletResponse response, @NonNull String code,
            @NonNull String state, String error, String errorDescription);

    /**
     * Tries to load authorized client from db. If that attempt fails, will 
     * try to resolve the issue by either updating the access token using 
     * appropriate refresh token if that option is enabled in {@link ClientRegistration}
     * or redirect to {@link #authorize} and begin authorization proccess.
     *
     * @param authentication contains user data
     * @param request for establishing origin
     * @param response for redirecting
     * @param clientRegistration contains required provider data
     * @return instance of {@code OAuth2AuthorizedClient} or {@code null} 
     * if redirect was initiated
     */
    OAuth2AuthorizedClient loadAuthorizedClient(@NonNull Authentication authentication, 
            @NonNull HttpServletRequest request, @NonNull HttpServletResponse response, 
            @NonNull ClientRegistration clientRegistration);

    /**
     * Permanently deletes this {@link OAuth2AuthorizedClient}. This method should be called when
     * user manualy logs out of service or is removed. It should not be called when
     * token expires or in any other case.
     * @param authorizedClient which needs to be deleted
     */
    void deleteAuthorizedClient(OAuth2AuthorizedClient authorizedClient);
}
