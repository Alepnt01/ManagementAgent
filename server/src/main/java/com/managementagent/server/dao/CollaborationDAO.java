package com.managementagent.server.dao;

import com.managementagent.server.model.ChatMessage;
import com.managementagent.server.model.ClientContact;
import com.managementagent.server.model.Employee;
import com.managementagent.server.model.Team;

import java.util.List;
import java.util.Optional;

/**
 * DAO responsible for collaboration-related entities such as teams, employees,
 * chat history and email logs.
 */
public interface CollaborationDAO {

    List<Team> findAllTeams();

    List<ChatMessage> findMessagesByTeam(long teamId);

    ChatMessage saveMessage(long teamId, long employeeId, String message);

    List<Employee> findAllEmployees();

    Optional<Employee> findEmployeeById(long employeeId);

    boolean isEmployeeMemberOfTeam(long teamId, long employeeId);

    List<ClientContact> findAllClients();

    Optional<ClientContact> findClientById(long clientId);

    void logEmail(long employeeId, long clientId, String subject, String body);
}
