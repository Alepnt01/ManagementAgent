package com.managementagent.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.managementagent.client.model.AgentPayload;
import com.managementagent.client.model.ChatMessagePayload;
import com.managementagent.client.model.ClientContactPayload;
import com.managementagent.client.model.EmailRequestPayload;
import com.managementagent.client.model.EmployeePayload;
import com.managementagent.client.model.TeamPayload;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * REST client that communicates with the Management Agent server.
 */
public class AgentApiClient {

    private final String baseUrl;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private final HttpClient httpClient;
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    public AgentApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public CompletableFuture<List<AgentPayload>> loadAgents() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/agents"))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApplyAsync(response -> readValue(response.body(), new TypeReference<>() {
                }), executorService);
    }

    public CompletableFuture<AgentPayload> createAgent(AgentPayload payload) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/agents"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(serialize(payload), StandardCharsets.UTF_8))
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApplyAsync(response -> readValue(response.body(), AgentPayload.class), executorService);
    }

    public CompletableFuture<AgentPayload> updateAgent(long id, AgentPayload payload) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/agents/" + id))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(serialize(payload), StandardCharsets.UTF_8))
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApplyAsync(response -> readValue(response.body(), AgentPayload.class), executorService);
    }

    public CompletableFuture<Void> deleteAgent(long id) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/agents/" + id))
                .DELETE()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .thenAcceptAsync(response -> {
                }, executorService);
    }

    public CompletableFuture<List<TeamPayload>> loadTeams() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/collaboration/teams"))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApplyAsync(response -> readValue(response.body(), new TypeReference<List<TeamPayload>>() {
                }), executorService);
    }

    public CompletableFuture<List<EmployeePayload>> loadEmployees() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/collaboration/employees"))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApplyAsync(response -> readValue(response.body(), new TypeReference<List<EmployeePayload>>() {
                }), executorService);
    }

    public CompletableFuture<List<ClientContactPayload>> loadClients() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/collaboration/clients"))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApplyAsync(response -> readValue(response.body(), new TypeReference<List<ClientContactPayload>>() {
                }), executorService);
    }

    public CompletableFuture<List<ChatMessagePayload>> loadChatMessages(long teamId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/collaboration/teams/" + teamId + "/messages"))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApplyAsync(response -> readValue(response.body(), new TypeReference<List<ChatMessagePayload>>() {
                }), executorService);
    }

    public CompletableFuture<ChatMessagePayload> sendChatMessage(long teamId, ChatMessagePayload payload) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/collaboration/teams/" + teamId + "/messages"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(writeValue(payload), StandardCharsets.UTF_8))
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApplyAsync(response -> readValue(response.body(), ChatMessagePayload.class), executorService);
    }

    public CompletableFuture<Void> sendEmail(EmailRequestPayload payload) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/communications/email"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(writeValue(payload), StandardCharsets.UTF_8))
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .thenAcceptAsync(response -> {
                }, executorService);
    }

    private <T> T readValue(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to parse response", e);
        }
    }

    private <T> T readValue(String json, TypeReference<T> reference) {
        try {
            return objectMapper.readValue(json, reference);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to parse response", e);
        }
    }

    private String writeValue(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to serialize payload", e);
        }
    }
}
