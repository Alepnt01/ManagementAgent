package com.managementagent.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.managementagent.server.model.ChatMessage;
import com.managementagent.server.service.TeamService;
import io.javalin.Javalin;
import io.javalin.http.Context;

/**
 * REST controller for teams, members and chat messages.
 */
public class CollaborationController {

    private final TeamService teamService;
    private final ObjectMapper objectMapper;

    public CollaborationController(TeamService teamService, ObjectMapper objectMapper) {
        this.teamService = teamService;
        this.objectMapper = objectMapper;
    }

    public void registerRoutes(Javalin app) {
        app.get("/collaboration/teams", this::getTeams);
        app.get("/collaboration/employees", this::getEmployees);
        app.get("/collaboration/clients", this::getClients);
        app.get("/collaboration/teams/{teamId}/messages", this::getMessages);
        app.post("/collaboration/teams/{teamId}/messages", this::createMessage);
    }

    private void getTeams(Context ctx) {
        teamService.loadTeamsAsync()
                .thenAccept(ctx::json)
                .exceptionally(throwable -> handleError(ctx, throwable));
    }

    private void getEmployees(Context ctx) {
        teamService.loadEmployeesAsync()
                .thenAccept(ctx::json)
                .exceptionally(throwable -> handleError(ctx, throwable));
    }

    private void getClients(Context ctx) {
        teamService.loadClientsAsync()
                .thenAccept(ctx::json)
                .exceptionally(throwable -> handleError(ctx, throwable));
    }

    private void getMessages(Context ctx) {
        long teamId = Long.parseLong(ctx.pathParam("teamId"));
        teamService.loadMessagesAsync(teamId)
                .thenAccept(ctx::json)
                .exceptionally(throwable -> handleError(ctx, throwable));
    }

    private void createMessage(Context ctx) {
        long teamId = Long.parseLong(ctx.pathParam("teamId"));
        ChatMessage message = readBody(ctx, ChatMessage.class);
        if (message.getSenderId() == null) {
            throw new IllegalArgumentException("Sender identifier is required");
        }
        if (message.getMessage() == null || message.getMessage().isBlank()) {
            throw new IllegalArgumentException("Message content cannot be empty");
        }
        teamService.sendMessageAsync(teamId, message.getSenderId(), message.getMessage())
                .thenAccept(created -> ctx.status(201).json(created))
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
