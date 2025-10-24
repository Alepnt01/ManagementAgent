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
 * Servizio applicativo che orchestra le chiamate ai DAO e gestisce l'elaborazione asincrona.
 */
public class AgentService {

    private final AgentDAO agentDAO;
    private final AgentFactory agentFactory;
    private final AgentEventPublisher eventPublisher;
    // Pool di thread dedicato alle operazioni non bloccanti sugli agenti.
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    public AgentService(AgentDAO agentDAO, AgentFactory agentFactory, AgentEventPublisher eventPublisher) {
        // Inietto le dipendenze principali tramite il costruttore per mantenere l'approccio OO.
        this.agentDAO = agentDAO;
        this.agentFactory = agentFactory;
        this.eventPublisher = eventPublisher;
    }

    public CompletableFuture<List<Agent>> getAllAgentsAsync() {
        // Recupera tutti gli agenti senza bloccare il thread chiamante.
        return CompletableFuture.supplyAsync(agentDAO::findAll, executorService);
    }

    public CompletableFuture<Optional<Agent>> getAgentByIdAsync(long id) {
        // Carica un singolo agente per id sfruttando il pool asincrono.
        return CompletableFuture.supplyAsync(() -> agentDAO.findById(id), executorService);
    }

    public CompletableFuture<Agent> createAgentAsync(AgentRequest request) {
        // Crea un nuovo agente e notifica gli observer dopo la persistenza.
        return CompletableFuture.supplyAsync(() -> {
            Agent agent = agentFactory.createAgent(request);
            Agent saved = agentDAO.save(agent);
            eventPublisher.publishAgentCreated(saved);
            return saved;
        }, executorService);
    }

    public CompletableFuture<Optional<Agent>> updateAgentAsync(long id, AgentRequest request) {
        // Aggiorna le informazioni di un agente esistente e invia l'evento di modifica.
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
        // Elimina l'agente se presente e propaga l'evento di cancellazione.
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
        // Spegne il pool di thread quando il server viene arrestato.
        executorService.shutdown();
    }
}
