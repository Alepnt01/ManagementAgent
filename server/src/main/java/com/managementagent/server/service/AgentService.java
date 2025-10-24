package com.managementagent.server.service;

import com.managementagent.server.dao.AgentDAO;
import com.managementagent.server.factory.AgentFactory;
import com.managementagent.server.model.Agent;
import com.managementagent.server.model.AgentRequest;
import com.managementagent.server.observer.AgentEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service layer orchestrating DAO calls and async processing.
 */
public class AgentService {

    private final AgentDAO agentDAO;
    private final AgentFactory agentFactory;
    private final AgentEventPublisher eventPublisher;
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    public AgentService(AgentDAO agentDAO, AgentFactory agentFactory, AgentEventPublisher eventPublisher) {
        this.agentDAO = agentDAO;
        this.agentFactory = agentFactory;
        this.eventPublisher = eventPublisher;
    }

    public CompletableFuture<List<Agent>> getAllAgentsAsync() {
        return CompletableFuture.supplyAsync(agentDAO::findAll, executorService);
    }

    public CompletableFuture<Optional<Agent>> getAgentByIdAsync(long id) {
        return CompletableFuture.supplyAsync(() -> agentDAO.findById(id), executorService);
    }

    public CompletableFuture<Agent> createAgentAsync(AgentRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            Agent agent = agentFactory.createAgent(request);
            Agent saved = agentDAO.save(agent);
            eventPublisher.publishAgentCreated(saved);
            return saved;
        }, executorService);
    }

    public CompletableFuture<Optional<Agent>> updateAgentAsync(long id, AgentRequest request) {
        return CompletableFuture.supplyAsync(() -> agentDAO.findById(id).map(existing -> {
            existing.setCode(request.getCode());
            existing.setName(request.getName());
            existing.setEmail(request.getEmail());
            existing.setPhone(request.getPhone());
            existing.setRegion(request.getRegion());
            existing.setStatus(request.getStatus());
            Agent updated = agentDAO.update(id, existing);
            eventPublisher.publishAgentUpdated(updated);
            return updated;
        }), executorService);
    }

    public CompletableFuture<Boolean> deleteAgentAsync(long id) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<Agent> agent = agentDAO.findById(id);
            agent.ifPresent(a -> {
                agentDAO.delete(id);
                eventPublisher.publishAgentDeleted(id);
            });
            return agent.isPresent();
        }, executorService);
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
