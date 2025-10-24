package com.managementagent.client.controller;

import com.managementagent.client.model.Agent;
import com.managementagent.client.model.AgentPayload;
import com.managementagent.client.service.AgentApiClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Controller mediating between the JavaFX view and the REST client.
 */
public class AgentController {

    private final AgentApiClient apiClient;
    private final ObservableList<Agent> agents = FXCollections.observableArrayList();

    public AgentController(AgentApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ObservableList<Agent> getAgents() {
        return agents;
    }

    public void refreshAgents() {
        apiClient.loadAgents().thenAccept(this::mapAndPopulate);
    }

    public CompletableFuture<Void> createAgent(String code, String name, String email, String phone, String region, String status) {
        AgentPayload payload = new AgentPayload();
        payload.setCode(code);
        payload.setName(name);
        payload.setEmail(email);
        payload.setPhone(phone);
        payload.setRegion(region);
        payload.setStatus(status);
        return apiClient.createAgent(payload)
                .thenAccept(agent -> Platform.runLater(() -> agents.add(convert(agent))));
    }

    public CompletableFuture<Void> updateAgent(Agent agent) {
        AgentPayload payload = new AgentPayload();
        payload.setCode(agent.getCode());
        payload.setName(agent.getName());
        payload.setEmail(agent.getEmail());
        payload.setPhone(agent.getPhone());
        payload.setRegion(agent.getRegion());
        payload.setStatus(agent.getStatus());
        return apiClient.updateAgent(agent.getId(), payload)
                .thenAccept(updated -> Platform.runLater(() -> {
                    int index = agents.indexOf(agent);
                    if (index >= 0) {
                        agents.set(index, convert(updated));
                    }
                }));
    }

    public CompletableFuture<Void> deleteAgent(Agent agent) {
        return apiClient.deleteAgent(agent.getId())
                .thenAccept(v -> Platform.runLater(() -> agents.remove(agent)));
    }

    private void mapAndPopulate(List<AgentPayload> payloads) {
        Platform.runLater(() -> {
            agents.clear();
            payloads.stream().map(this::convert).forEach(agents::add);
        });
    }

    private Agent convert(AgentPayload payload) {
        Agent agent = new Agent();
        agent.setId(payload.getId());
        agent.setCode(payload.getCode());
        agent.setName(payload.getName());
        agent.setEmail(payload.getEmail());
        agent.setPhone(payload.getPhone());
        agent.setRegion(payload.getRegion());
        agent.setStatus(payload.getStatus());
        return agent;
    }
}
