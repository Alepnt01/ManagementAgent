package com.managementagent.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.managementagent.server.model.EmailRequest;
import com.managementagent.server.service.MailService;
import io.javalin.Javalin;
import io.javalin.http.Context;

/**
 * REST controller exposing email sending endpoint.
 */
public class MailController {

    private final MailService mailService;
    private final ObjectMapper objectMapper;

    public MailController(MailService mailService, ObjectMapper objectMapper) {
        this.mailService = mailService;
        this.objectMapper = objectMapper;
    }

    public void registerRoutes(Javalin app) {
        app.post("/communications/email", this::sendEmail);
    }

    private void sendEmail(Context ctx) {
        EmailRequest request = readBody(ctx, EmailRequest.class);
        if (request.getSubject() == null || request.getSubject().isBlank()) {
            throw new IllegalArgumentException("Subject is required");
        }
        if (request.getBody() == null || request.getBody().isBlank()) {
            throw new IllegalArgumentException("Body is required");
        }
        mailService.sendEmailAsync(request)
                .thenAccept(aVoid -> ctx.status(202))
                .exceptionally(throwable -> handleError(ctx, throwable));
    }

    private <T> T readBody(Context ctx, Class<T> clazz) {
        try {
            return objectMapper.readValue(ctx.body(), clazz);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid request body", e);
        }
    }

    private Void handleError(Context ctx, Throwable throwable) {
        Throwable cause = throwable.getCause() != null ? throwable.getCause() : throwable;
        if (cause instanceof IllegalArgumentException) {
            ctx.status(400).result("Bad request: " + cause.getMessage());
        } else {
            ctx.status(500).result("Server error: " + cause.getMessage());
        }
        return null;
    }
}
