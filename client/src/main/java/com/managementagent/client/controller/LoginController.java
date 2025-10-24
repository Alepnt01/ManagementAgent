package com.managementagent.client.controller;

import com.managementagent.client.service.AgentApiClient;

import java.util.concurrent.CompletableFuture;

/**
 * Controller that orchestrates the login flow for the JavaFX client.
 */
public class LoginController {

    private final AgentApiClient apiClient;

    public LoginController(AgentApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public CompletableFuture<Void> login(String username, String password) {
        return apiClient.login(username, password).thenAccept(token -> {
        });
    }
}
