package com.managementagent.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.managementagent.server.model.ChatMessage;
import com.managementagent.server.service.TeamService;
import io.javalin.Javalin;
import io.javalin.http.Context;

/**
 * Controller REST dedicato a team, membri e messaggi di chat.
 */
public class CollaborationController {

    private final TeamService teamService;
    private final ObjectMapper objectMapper;

    public CollaborationController(TeamService teamService, ObjectMapper objectMapper) {
        // Inietta il servizio di dominio e il mapper JSON da riutilizzare nelle risposte.
        this.teamService = teamService;
        this.objectMapper = objectMapper;
    }

    public void registerRoutes(Javalin app) {
        // Espone le rotte HTTP del modulo collaborazione.
        app.get("/collaboration/teams", this::getTeams);
        app.get("/collaboration/employees", this::getEmployees);
        app.get("/collaboration/clients", this::getClients);
        app.get("/collaboration/teams/{teamId}/messages", this::getMessages);
        app.post("/collaboration/teams/{teamId}/messages", this::createMessage);
    }

    private void getTeams(Context ctx) {
        // Recupera l'elenco dei team in modo asincrono e restituisce JSON.
        teamService.loadTeamsAsync()
                .thenAccept(ctx::json)
                .exceptionally(throwable -> handleError(ctx, throwable));
    }

    private void getEmployees(Context ctx) {
        // Restituisce i dipendenti disponibili per la chat.
        teamService.loadEmployeesAsync()
                .thenAccept(ctx::json)
                .exceptionally(throwable -> handleError(ctx, throwable));
    }

    private void getClients(Context ctx) {
        // Restituisce i contatti clienti che possono ricevere email.
        teamService.loadClientsAsync()
                .thenAccept(ctx::json)
                .exceptionally(throwable -> handleError(ctx, throwable));
    }

    private void getMessages(Context ctx) {
        // Recupera i messaggi associati a un team specifico.
        long teamId = Long.parseLong(ctx.pathParam("teamId"));
        teamService.loadMessagesAsync(teamId)
                .thenAccept(ctx::json)
                .exceptionally(throwable -> handleError(ctx, throwable));
    }

    private void createMessage(Context ctx) {
        // Crea un nuovo messaggio nella chat del team indicato.
        long teamId = Long.parseLong(ctx.pathParam("teamId"));
        ChatMessage message = readBody(ctx, ChatMessage.class);
        if (message.getSenderId() == null) {
            // Valido la presenza del mittente.
            throw new IllegalArgumentException("Sender identifier is required");
        }
        if (message.getMessage() == null || message.getMessage().isBlank()) {
            // Evito di registrare messaggi vuoti.
            throw new IllegalArgumentException("Message content cannot be empty");
        }
        teamService.sendMessageAsync(teamId, message.getSenderId(), message.getMessage())
                .thenAccept(created -> ctx.status(201).json(created))
                .exceptionally(throwable -> handleError(ctx, throwable));
    }

    private <T> T readBody(Context ctx, Class<T> clazz) {
        try {
            // Deserializzo il corpo della richiesta con ObjectMapper per riutilizzare le classi di dominio.
            return objectMapper.readValue(ctx.body(), clazz);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid request body", e);
        }
    }

    private Void handleError(Context ctx, Throwable throwable) {
        // Gestisce gli errori propagati dalle operazioni asincrone restituendo il codice HTTP corretto.
        Throwable cause = throwable.getCause() != null ? throwable.getCause() : throwable;
        if (cause instanceof IllegalArgumentException) {
            ctx.status(400).result("Bad request: " + cause.getMessage());
        } else {
            ctx.status(500).result("Server error: " + cause.getMessage());
        }
        return null;
    }
}
