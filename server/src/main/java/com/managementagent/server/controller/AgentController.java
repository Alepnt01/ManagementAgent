package com.managementagent.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.managementagent.server.model.AgentRequest;
import com.managementagent.server.service.AgentService;
import io.javalin.Javalin;
import io.javalin.http.Context;

/**
 * REST controller exposing CRUD endpoints.
 */
public class AgentController {

    private final AgentService agentService;
    private final ObjectMapper objectMapper;

    public AgentController(AgentService agentService, ObjectMapper objectMapper) {
        this.agentService = agentService;
        this.objectMapper = objectMapper;
    }

    public void registerRoutes(Javalin app) {
        app.get("/agents", this::getAllAgents);
        app.get("/agents/{id}", this::getAgentById);
        app.post("/agents", this::createAgent);
        app.put("/agents/{id}", this::updateAgent);
        app.delete("/agents/{id}", this::deleteAgent);
    }

    private void getAllAgents(Context ctx) {
        ctx.future(agentService.getAllAgentsAsync()
                .thenAccept(ctx::json)
                .exceptionally(throwable -> handleError(ctx, throwable)));
    }

    private void getAgentById(Context ctx) {
        long id = Long.parseLong(ctx.pathParam("id"));
        ctx.future(agentService.getAgentByIdAsync(id)
                .thenAccept(optional -> optional.ifPresentOrElse(ctx::json, () -> ctx.status(404)))
                .exceptionally(throwable -> handleError(ctx, throwable)));
    }

    private void createAgent(Context ctx) {
        AgentRequest request = readBody(ctx, AgentRequest.class);
        ctx.future(agentService.createAgentAsync(request)
                .thenAccept(agent -> ctx.status(201).json(agent))
                .exceptionally(throwable -> handleError(ctx, throwable)));
    }

    private void updateAgent(Context ctx) {
        long id = Long.parseLong(ctx.pathParam("id"));
        AgentRequest request = readBody(ctx, AgentRequest.class);
        ctx.future(agentService.updateAgentAsync(id, request)
                .thenAccept(optional -> optional.ifPresentOrElse(ctx::json, () -> ctx.status(404)))
                .exceptionally(throwable -> handleError(ctx, throwable)));
    }

    private void deleteAgent(Context ctx) {
        long id = Long.parseLong(ctx.pathParam("id"));
        ctx.future(agentService.deleteAgentAsync(id)
                .thenAccept(deleted -> {
                    if (deleted) {
                        ctx.status(204);
                    } else {
                        ctx.status(404);
                    }
                })
                .exceptionally(throwable -> handleError(ctx, throwable)));
    }

    private <T> T readBody(Context ctx, Class<T> clazz) {
        try {
            return objectMapper.readValue(ctx.body(), clazz);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid request body", e);
        }
    }

    private Void handleError(Context ctx, Throwable throwable) {
        ctx.status(500).result("Server error: " + throwable.getMessage());
        return null;
    }
}
