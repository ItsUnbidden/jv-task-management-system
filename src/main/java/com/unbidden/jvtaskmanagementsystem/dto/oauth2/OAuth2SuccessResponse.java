package com.unbidden.jvtaskmanagementsystem.dto.oauth2;

import lombok.Data;

@Data
public class OAuth2SuccessResponse {
    private String result;
    
    public OAuth2SuccessResponse(String result) {
        this.result = result;
    }
}
