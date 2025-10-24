package com.managementagent.server.service;

import com.managementagent.server.dao.CollaborationDAO;
import com.managementagent.server.model.ChatMessage;
import com.managementagent.server.model.ClientContact;
import com.managementagent.server.model.Employee;
import com.managementagent.server.model.Team;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service coordinating asynchronous access to team collaboration data.
 */
public class TeamService {

    private final CollaborationDAO collaborationDAO;
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    public TeamService(CollaborationDAO collaborationDAO) {
        this.collaborationDAO = collaborationDAO;
    }

    public CompletableFuture<List<Team>> loadTeamsAsync() {
        return CompletableFuture.supplyAsync(collaborationDAO::findAllTeams, executorService);
    }

    public CompletableFuture<List<Employee>> loadEmployeesAsync() {
        return CompletableFuture.supplyAsync(collaborationDAO::findAllEmployees, executorService);
    }

    public CompletableFuture<List<ClientContact>> loadClientsAsync() {
        return CompletableFuture.supplyAsync(collaborationDAO::findAllClients, executorService);
    }

    public CompletableFuture<List<ChatMessage>> loadMessagesAsync(long teamId) {
        return CompletableFuture.supplyAsync(() -> collaborationDAO.findMessagesByTeam(teamId), executorService);
    }

    public CompletableFuture<ChatMessage> sendMessageAsync(long teamId, long employeeId, String message) {
        return CompletableFuture.supplyAsync(() -> collaborationDAO.saveMessage(teamId, employeeId, message), executorService);
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
