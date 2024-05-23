package com.unbidden.jvtaskmanagementsystem.util;

import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import java.io.IOException;

public class BearerAuthentication implements HttpRequestInitializer, HttpExecuteInterceptor {
    private String token;

    public BearerAuthentication(String token) {
        this.token = token;
    }

    @Override
    public void initialize(HttpRequest request) throws IOException {
        request.setInterceptor(this);
    }

    @Override
    public void intercept(HttpRequest request) throws IOException {
        request.getHeaders().setAuthorization("Bearer " + token);
    }
}
