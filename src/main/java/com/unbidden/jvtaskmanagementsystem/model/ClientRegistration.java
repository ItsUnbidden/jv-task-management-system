package com.unbidden.jvtaskmanagementsystem.model;

import lombok.Data;
import lombok.ToString;

@Data
public class ClientRegistration {
    private String clientName;

    private String clientId;

    @ToString.Exclude
    private String clientSecret;

    private String redirectUri;

    private String authorizationUri;

    private String tokenUri;

    private Boolean useRefreshTokens;
}
