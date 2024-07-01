package com.unbidden.jvtaskmanagementsystem.controller;

import com.unbidden.jvtaskmanagementsystem.dto.oauth2.OAuth2SuccessResponse;
import com.unbidden.jvtaskmanagementsystem.model.ClientRegistration;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.service.oauth2.OAuth2Service;
import com.unbidden.jvtaskmanagementsystem.util.EntityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth2/connect")
@Tag(name = "OAuth2 endpoints")
public class OAuth2Controller {
    private final EntityUtil entityUtil;

    private final OAuth2Service oauth2Service;

    @GetMapping("/dropbox")
    @Operation(
            summary = "Start dropbox authorization flow",
            responses = {
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "302",
                    description = "Redirect to dropbox authorization page"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "400",
                    description = "Some issue occurred. This is not necessarily a user "
                        + "problem but can be basicaly anything"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "401",
                    description = "Unauthorized. Might be some other issue as well")
            }
    )
    public void initiateDropboxAuthorization(Authentication authentication, 
            HttpServletResponse response) {
        ClientRegistration clientRegistration = entityUtil.getClientRegistrationByName("dropbox");
        oauth2Service.authorize((User)authentication.getPrincipal(), response, 
                clientRegistration);
    }

    @GetMapping("/google")
    @Operation(
            summary = "Start google authorization flow",
            responses = {
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "302",
                    description = "Redirect to google authorization page"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "400",
                    description = "Some issue occurred. This is not necessarily a user "
                        + "problem but can be basicaly anything"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "401",
                    description = "Unauthorized. Might be some other issue as well")
            }
    )
    public void initiateGoogleAuthorization(Authentication authentication, 
            HttpServletResponse response) {
        ClientRegistration clientRegistration = entityUtil.getClientRegistrationByName("google");
        oauth2Service.authorize((User)authentication.getPrincipal(), response, 
                clientRegistration);
    }
    
    @GetMapping("/code")
    @Operation(
            summary = "OAuth2 callback endpoint for 3rd party services to redirect to",
            description = "Users are not supposed to directly call this endpoint",
            responses = {
                @ApiResponse(
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = OAuth2SuccessResponse.class)),
                    responseCode = "200",
                    description = "Success message"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "400",
                    description = "Some issue occurred. This is not necessarily a user "
                        + "problem but can be basicaly anything"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "401",
                    description = "Unauthorized. Might be some other issue as well")
            }
    )
    public OAuth2SuccessResponse callback(HttpServletResponse response,
            @Parameter(
                description = "Authorization code provided by the authorization server"
            )
            @NonNull @RequestParam String code,
            @Parameter(
                description = "Authorization request id that was generated by the OAuth2"
                    + " service. Required primarily for user identification"
            )
            @NonNull @RequestParam String state,
            @Parameter(
                description = "Small summary of the potential error"
            )
            String error,
            @Parameter(
                description = "More user-friendly summary of the potential error"
            )
            String errorDescription) {
        return oauth2Service.callback(response, code, state, error, errorDescription);
    }
}
