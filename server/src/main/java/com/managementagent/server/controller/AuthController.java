package com.managementagent.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.managementagent.server.model.LoginRequest;
import com.managementagent.server.model.LoginResponse;
import com.managementagent.server.service.AuthService;
import io.javalin.Javalin;
import io.javalin.http.Context;

/**
 * REST controller exposing authentication endpoints.
 */
public class AuthController {

    private final AuthService authService;
    private final ObjectMapper objectMapper;

    public AuthController(AuthService authService, ObjectMapper objectMapper) {
        this.authService = authService;
        this.objectMapper = objectMapper;
    }

    public void registerRoutes(Javalin app) {
        app.post("/auth/login", this::login);
        app.post("/auth/logout", this::logout);
    }

    private void login(Context ctx) {
        if (!authService.isAuthenticationEnabled()) {
            ctx.status(503).result("Authentication is disabled");
            return;
        }
        LoginRequest request = readBody(ctx, LoginRequest.class);
        authService.authenticate(request.getUsername(), request.getPassword())
                .ifPresentOrElse(token -> ctx.json(new LoginResponse(request.getUsername(), token)),
                        () -> ctx.status(401).result("Invalid credentials"));
    }

    private void logout(Context ctx) {
        String authorization = ctx.header("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring("Bearer ".length());
            authService.invalidate(token);
        }
        ctx.status(204);
    }

    private <T> T readBody(Context ctx, Class<T> clazz) {
        try {
            return objectMapper.readValue(ctx.body(), clazz);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid request body", e);
        }
    }
}
