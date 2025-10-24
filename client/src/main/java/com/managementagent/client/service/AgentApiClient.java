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
 * Client REST che dialoga con il server Management Agent.
 */
public class AgentApiClient {

    private final String baseUrl;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private final HttpClient httpClient;
    // Esecutore dedicato al parsing JSON per non bloccare il thread JavaFX.
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    public AgentApiClient(String baseUrl) {
        // Base URL del server remoto, ad esempio http://localhost:7070.
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newBuilder()
                // Utilizzo HTTP/1.1 per massima compatibilità.
                .version(HttpClient.Version.HTTP_1_1)
                // Timeout breve per evitare attese infinite durante le connessioni.
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public CompletableFuture<List<AgentPayload>> loadAgents() {
        // Richiede tutti gli agenti disponibili al server.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/agents"))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApplyAsync(response -> readValue(response.body(), new TypeReference<>() {
                }), executorService);
    }

    public CompletableFuture<AgentPayload> createAgent(AgentPayload payload) {
        // Invio POST per creare un nuovo agente persistente.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/agents"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(serialize(payload), StandardCharsets.UTF_8))
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApplyAsync(response -> readValue(response.body(), AgentPayload.class), executorService);
    }

    public CompletableFuture<AgentPayload> updateAgent(long id, AgentPayload payload) {
        // Aggiorna un agente esistente tramite chiamata PUT.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/agents/" + id))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(serialize(payload), StandardCharsets.UTF_8))
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApplyAsync(response -> readValue(response.body(), AgentPayload.class), executorService);
    }

    public CompletableFuture<Void> deleteAgent(long id) {
        // Elimina un agente sfruttando il verbo HTTP DELETE.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/agents/" + id))
                .DELETE()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .thenAcceptAsync(response -> {
                }, executorService);
    }

    public CompletableFuture<List<TeamPayload>> loadTeams() {
        // Recupera i team per popolare le viste collaborative.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/collaboration/teams"))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApplyAsync(response -> readValue(response.body(), new TypeReference<List<TeamPayload>>() {
                }), executorService);
    }

    public CompletableFuture<List<EmployeePayload>> loadEmployees() {
        // Scarica i dipendenti che possono partecipare alle chat.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/collaboration/employees"))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApplyAsync(response -> readValue(response.body(), new TypeReference<List<EmployeePayload>>() {
                }), executorService);
    }

    public CompletableFuture<List<ClientContactPayload>> loadClients() {
        // Ottiene i contatti dei clienti gestiti dagli agenti.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/collaboration/clients"))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApplyAsync(response -> readValue(response.body(), new TypeReference<List<ClientContactPayload>>() {
                }), executorService);
    }

    public CompletableFuture<List<ChatMessagePayload>> loadChatMessages(long teamId) {
        // Recupera i messaggi di chat per il team specificato.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/collaboration/teams/" + teamId + "/messages"))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApplyAsync(response -> readValue(response.body(), new TypeReference<List<ChatMessagePayload>>() {
                }), executorService);
    }

    public CompletableFuture<ChatMessagePayload> sendChatMessage(long teamId, ChatMessagePayload payload) {
        // Spedisce un nuovo messaggio nella chat REST del team.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/collaboration/teams/" + teamId + "/messages"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(writeValue(payload), StandardCharsets.UTF_8))
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApplyAsync(response -> readValue(response.body(), ChatMessagePayload.class), executorService);
    }

    public CompletableFuture<Void> sendEmail(EmailRequestPayload payload) {
        // Inoltra una richiesta di invio email al server.
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
            // Deserializza il JSON in una classe specifica.
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to parse response", e);
        }
    }

    private <T> T readValue(String json, TypeReference<T> reference) {
        try {
            // Deserializza il JSON utilizzando un TypeReference per strutture generiche.
            return objectMapper.readValue(json, reference);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to parse response", e);
        }
    }

    private String writeValue(Object payload) {
        try {
            // Serializza il payload Java in una stringa JSON.
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to serialize payload", e);
        }
    }
}
