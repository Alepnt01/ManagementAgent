package com.managementagent.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.managementagent.client.model.AgentPayload;

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
    private final ObjectMapper objectMapper = new ObjectMapper();
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
                .thenApplyAsync(response -> parseList(response.body()), executorService);
    }

    public CompletableFuture<AgentPayload> createAgent(AgentPayload payload) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/agents"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(serialize(payload), StandardCharsets.UTF_8))
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApplyAsync(response -> parse(response.body()), executorService);
    }

    public CompletableFuture<AgentPayload> updateAgent(long id, AgentPayload payload) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/agents/" + id))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(serialize(payload), StandardCharsets.UTF_8))
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApplyAsync(response -> parse(response.body()), executorService);
    }

    public CompletableFuture<Void> deleteAgent(long id) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/agents/" + id))
                .DELETE()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .thenAcceptAsync(response -> {}, executorService);
    }

    private List<AgentPayload> parseList(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new IllegalStateException("Unable to parse agents", e);
        }
    }

    private AgentPayload parse(String json) {
        try {
            return objectMapper.readValue(json, AgentPayload.class);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to parse agent", e);
        }
    }

    private String serialize(AgentPayload payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to serialize agent", e);
        }
    }
}
