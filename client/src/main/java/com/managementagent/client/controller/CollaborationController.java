package com.managementagent.client.controller;

import com.managementagent.client.model.ChatMessage;
import com.managementagent.client.model.ChatMessagePayload;
import com.managementagent.client.model.ClientContact;
import com.managementagent.client.model.ClientContactPayload;
import com.managementagent.client.model.EmailRequestPayload;
import com.managementagent.client.model.Employee;
import com.managementagent.client.model.EmployeePayload;
import com.managementagent.client.model.Team;
import com.managementagent.client.model.TeamPayload;
import com.managementagent.client.service.AgentApiClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Controller responsible for collaboration features (chat & email).
 */
public class CollaborationController {

    private final AgentApiClient apiClient;
    private final ObservableList<Team> teams = FXCollections.observableArrayList();
    private final ObservableList<Employee> employees = FXCollections.observableArrayList();
    private final ObservableList<ClientContact> clients = FXCollections.observableArrayList();
    private final ObservableList<ChatMessage> messages = FXCollections.observableArrayList();

    public CollaborationController(AgentApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ObservableList<Team> getTeams() {
        return teams;
    }

    public ObservableList<Employee> getEmployees() {
        return employees;
    }

    public ObservableList<ClientContact> getClients() {
        return clients;
    }

    public ObservableList<ChatMessage> getMessages() {
        return messages;
    }

    public void refreshTeams() {
        apiClient.loadTeams().thenAccept(this::mapTeams);
    }

    public void refreshEmployees() {
        apiClient.loadEmployees().thenAccept(this::mapEmployees);
    }

    public void refreshClients() {
        apiClient.loadClients().thenAccept(this::mapClients);
    }

    public void refreshMessages(long teamId) {
        apiClient.loadChatMessages(teamId).thenAccept(payloads -> Platform.runLater(() -> {
            messages.clear();
            payloads.stream().map(this::convertMessage).forEach(messages::add);
        }));
    }

    public CompletableFuture<Void> sendChatMessage(Team team, Employee sender, String text) {
        ChatMessagePayload payload = new ChatMessagePayload();
        payload.setSenderId(sender.getId());
        payload.setMessage(text);
        return apiClient.sendChatMessage(team.getId(), payload)
                .thenAccept(response -> Platform.runLater(() -> messages.add(convertMessage(response))));
    }

    public CompletableFuture<Void> sendEmail(Employee sender, ClientContact client, String subject, String body) {
        EmailRequestPayload payload = new EmailRequestPayload();
        payload.setEmployeeId(sender.getId());
        payload.setClientId(client.getId());
        payload.setSubject(subject);
        payload.setBody(body);
        return apiClient.sendEmail(payload);
    }

    private void mapTeams(List<TeamPayload> payloads) {
        Platform.runLater(() -> {
            teams.clear();
            payloads.stream().map(this::convertTeam).forEach(teams::add);
        });
    }

    private void mapEmployees(List<EmployeePayload> payloads) {
        Platform.runLater(() -> {
            employees.clear();
            payloads.stream().map(this::convertEmployee).forEach(employees::add);
        });
    }

    private void mapClients(List<ClientContactPayload> payloads) {
        Platform.runLater(() -> {
            clients.clear();
            payloads.stream().map(this::convertClient).forEach(clients::add);
        });
    }

    private Team convertTeam(TeamPayload payload) {
        Team team = new Team();
        if (payload.getId() != null) {
            team.setId(payload.getId());
        }
        team.setName(payload.getName());
        team.setDescription(payload.getDescription());
        List<EmployeePayload> members = payload.getMembers() != null ? payload.getMembers() : List.of();
        team.getMembers().setAll(members.stream().map(this::convertEmployee).toList());
        return team;
    }

    private Employee convertEmployee(EmployeePayload payload) {
        Employee employee = new Employee();
        if (payload.getId() != null) {
            employee.setId(payload.getId());
        }
        employee.setName(payload.getName());
        employee.setEmail(payload.getEmail());
        employee.setJobTitle(payload.getJobTitle());
        return employee;
    }

    private ClientContact convertClient(ClientContactPayload payload) {
        ClientContact contact = new ClientContact();
        if (payload.getId() != null) {
            contact.setId(payload.getId());
        }
        contact.setName(payload.getName());
        contact.setEmail(payload.getEmail());
        contact.setCompanyName(payload.getCompanyName());
        return contact;
    }

    private ChatMessage convertMessage(ChatMessagePayload payload) {
        ChatMessage message = new ChatMessage();
        if (payload.getId() != null) {
            message.setId(payload.getId());
        }
        if (payload.getTeamId() != null) {
            message.setTeamId(payload.getTeamId());
        }
        if (payload.getSenderId() != null) {
            message.setSenderId(payload.getSenderId());
        }
        message.setSenderName(payload.getSenderName());
        message.setMessage(payload.getMessage());
        message.setSentAt(payload.getSentAt());
        return message;
    }
}
