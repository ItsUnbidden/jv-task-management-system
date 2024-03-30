package com.unbidden.jvtaskmanagementsystem.service.util;

import java.util.Base64;
import org.springframework.stereotype.Component;

@Component
public class HttpClientUtil {
    public static final String AUTHORIZATION = "Authorization";

    public static final String LOCATION = "Location";

    public static final String CONTENT_TYPE = "Content-Type";

    public static final String APPLICATION_JSON = "application/json";

    public String getBasicAuthorizationHeader(String username, String password) {
        final String rawValue = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(rawValue.getBytes());
    }

    public String getBearerAuthorizationHeader(String token) {
        return "Bearer " + token;
    }
}
