package com.unbidden.jvtaskmanagementsystem.model;

import lombok.Data;

@Data
public class ClientRegistration {
    private String clientName;

    private String clientId;

    private String clientSecret;

    private String redirectUri;

    private String authorizationUri;

    private String tokenUri;

    private Boolean useRefreshTokens;

    private String defaultRedirectAfterCallback;
}
