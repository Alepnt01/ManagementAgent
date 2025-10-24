package com.managementagent.server.dao;

import com.managementagent.server.model.ChatMessage;
import com.managementagent.server.model.ClientContact;
import com.managementagent.server.model.Employee;
import com.managementagent.server.model.Team;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Concrete SQL Server implementation handling team collaboration.
 */
public class SqlServerCollaborationDAO implements CollaborationDAO {

    private final DataSource dataSource;

    public SqlServerCollaborationDAO() {
        this.dataSource = DatabaseConnectionManager.getInstance().getDataSource();
    }

    @Override
    public List<Team> findAllTeams() {
        String sql = "SELECT t.id AS team_id, t.name, t.description, " +
                "p.id AS person_id, p.full_name, p.email, p.phone, e.job_title " +
                "FROM teams t " +
                "LEFT JOIN team_members tm ON t.id = tm.team_id " +
                "LEFT JOIN employees e ON tm.employee_id = e.person_id " +
                "LEFT JOIN persons p ON e.person_id = p.id " +
                "ORDER BY t.name, p.full_name";
        Map<Long, Team> teams = new LinkedHashMap<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                long teamId = rs.getLong("team_id");
                Team team = teams.computeIfAbsent(teamId, id -> {
                    Team created = new Team();
                    created.setId(id);
                    created.setName(getString(rs, "name"));
                    created.setDescription(getString(rs, "description"));
                    return created;
                });
                long memberId = rs.getLong("person_id");
                if (!rs.wasNull()) {
                    Employee employee = new Employee();
                    employee.setId(memberId);
                    employee.setName(getString(rs, "full_name"));
                    employee.setEmail(getString(rs, "email"));
                    employee.setPhone(getString(rs, "phone"));
                    employee.setJobTitle(getString(rs, "job_title"));
                    team.getMembers().add(employee);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load teams", e);
        }
        return new ArrayList<>(teams.values());
    }

    @Override
    public List<ChatMessage> findMessagesByTeam(long teamId) {
        String sql = "SELECT m.id, m.team_id, m.sender_id, p.full_name, m.message, m.sent_at " +
                "FROM team_chat_messages m " +
                "JOIN persons p ON m.sender_id = p.id " +
                "WHERE m.team_id = ? ORDER BY m.sent_at";
        List<ChatMessage> messages = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, teamId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    messages.add(mapChatRow(rs));
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load chat messages for team " + teamId, e);
        }
        return messages;
    }

    @Override
    public ChatMessage saveMessage(long teamId, long employeeId, String message) {
        if (!isEmployeeMemberOfTeam(teamId, employeeId)) {
            throw new IllegalArgumentException("Employee " + employeeId + " is not part of team " + teamId);
        }
        String sql = "INSERT INTO team_chat_messages (team_id, sender_id, message) VALUES (?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, teamId);
            statement.setLong(2, employeeId);
            statement.setString(3, message);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    return findMessagesById(connection, id).orElseThrow(() ->
                            new IllegalStateException("Unable to fetch created chat message"));
                }
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to store chat message", e);
        }
        throw new IllegalStateException("Chat message creation failed for team " + teamId);
    }

    private Optional<ChatMessage> findMessagesById(Connection connection, long id) {
        String sql = "SELECT m.id, m.team_id, m.sender_id, p.full_name, m.message, m.sent_at " +
                "FROM team_chat_messages m JOIN persons p ON m.sender_id = p.id WHERE m.id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapChatRow(rs));
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to retrieve chat message " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Employee> findAllEmployees() {
        String sql = "SELECT e.person_id, p.full_name, p.email, p.phone, e.job_title " +
                "FROM employees e JOIN persons p ON e.person_id = p.id ORDER BY p.full_name";
        List<Employee> employees = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                employees.add(mapEmployee(rs));
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load employees", e);
        }
        return employees;
    }

    @Override
    public Optional<Employee> findEmployeeById(long employeeId) {
        String sql = "SELECT e.person_id, p.full_name, p.email, p.phone, e.job_title " +
                "FROM employees e JOIN persons p ON e.person_id = p.id WHERE e.person_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, employeeId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapEmployee(rs));
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to find employee " + employeeId, e);
        }
        return Optional.empty();
    }

    @Override
    public boolean isEmployeeMemberOfTeam(long teamId, long employeeId) {
        String sql = "SELECT COUNT(*) FROM team_members WHERE team_id = ? AND employee_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, teamId);
            statement.setLong(2, employeeId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to verify membership for employee " + employeeId, e);
        }
        return false;
    }

    @Override
    public List<ClientContact> findAllClients() {
        String sql = "SELECT c.person_id, p.full_name, p.email, p.phone, c.company_name, c.vat_number " +
                "FROM clients c JOIN persons p ON c.person_id = p.id ORDER BY p.full_name";
        List<ClientContact> clients = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                clients.add(mapClient(rs));
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load clients", e);
        }
        return clients;
    }

    @Override
    public Optional<ClientContact> findClientById(long clientId) {
        String sql = "SELECT c.person_id, p.full_name, p.email, p.phone, c.company_name, c.vat_number " +
                "FROM clients c JOIN persons p ON c.person_id = p.id WHERE c.person_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, clientId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapClient(rs));
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to find client " + clientId, e);
        }
        return Optional.empty();
    }

    @Override
    public void logEmail(long employeeId, long clientId, String subject, String body) {
        String sql = "INSERT INTO email_messages (employee_id, client_id, subject, body) VALUES (?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, employeeId);
            statement.setLong(2, clientId);
            statement.setString(3, subject);
            statement.setString(4, body);
            statement.executeUpdate();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to log email communication", e);
        }
    }

    private ChatMessage mapChatRow(ResultSet rs) throws Exception {
        ChatMessage message = new ChatMessage();
        message.setId(rs.getLong("id"));
        message.setTeamId(rs.getLong("team_id"));
        message.setSenderId(rs.getLong("sender_id"));
        message.setSenderName(getString(rs, "full_name"));
        message.setMessage(getString(rs, "message"));
        Timestamp timestamp = rs.getTimestamp("sent_at");
        message.setSentAt(timestamp != null ? timestamp.toLocalDateTime() : LocalDateTime.now());
        return message;
    }

    private Employee mapEmployee(ResultSet rs) throws Exception {
        Employee employee = new Employee();
        employee.setId(rs.getLong("person_id"));
        employee.setName(getString(rs, "full_name"));
        employee.setEmail(getString(rs, "email"));
        employee.setPhone(getString(rs, "phone"));
        employee.setJobTitle(getString(rs, "job_title"));
        return employee;
    }

    private ClientContact mapClient(ResultSet rs) throws Exception {
        ClientContact client = new ClientContact();
        client.setId(rs.getLong("person_id"));
        client.setName(getString(rs, "full_name"));
        client.setEmail(getString(rs, "email"));
        client.setPhone(getString(rs, "phone"));
        client.setCompanyName(getString(rs, "company_name"));
        client.setVatNumber(getString(rs, "vat_number"));
        return client;
    }

    private String getString(ResultSet rs, String column) {
        try {
            String value = rs.getString(column);
            return value != null ? value : "";
        } catch (Exception e) {
            return "";
        }
    }
}
