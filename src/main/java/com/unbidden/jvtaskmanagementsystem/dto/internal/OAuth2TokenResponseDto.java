package com.unbidden.jvtaskmanagementsystem.dto.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.unbidden.jvtaskmanagementsystem.dto.internal.dropbox.DropboxResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class OAuth2TokenResponseDto extends DropboxResponse {
    @JsonProperty("access_token")
    private String accessToken;
    
    @JsonProperty("expires_in")
    private Integer expiresIn;
    
    @JsonProperty("token_type")
    private String tokenType;
    
    @JsonProperty("refresh_token")
    private String refreshToken;

    private String scope;
}
