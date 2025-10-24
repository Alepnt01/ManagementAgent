package com.managementagent.server.dao;

import com.managementagent.server.model.Agent;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Concrete DAO implementation backed by SQL Server.
 */
public class SqlServerAgentDAO implements AgentDAO {

    private final DataSource dataSource;

    public SqlServerAgentDAO() {
        this.dataSource = DatabaseConnectionManager.getInstance().getDataSource();
    }

    @Override
    public List<Agent> findAll() {
        String sql = "SELECT a.person_id AS id, p.full_name AS name, p.email, p.phone, " +
                "a.code, a.region, a.status, a.last_update " +
                "FROM agents a JOIN persons p ON a.person_id = p.id ORDER BY a.person_id";
        List<Agent> agents = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                agents.add(mapRow(resultSet));
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to retrieve agents", e);
        }
        return agents;
    }

    @Override
    public Optional<Agent> findById(long id) {
        String sql = "SELECT a.person_id AS id, p.full_name AS name, p.email, p.phone, " +
                "a.code, a.region, a.status, a.last_update " +
                "FROM agents a JOIN persons p ON a.person_id = p.id WHERE a.person_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to retrieve agent with id " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public Agent save(Agent agent) {
        String insertPerson = "INSERT INTO persons (full_name, email, phone) VALUES (?, ?, ?)";
        String insertAgent = "INSERT INTO agents (person_id, code, region, status, last_update) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement personStatement = connection.prepareStatement(insertPerson, Statement.RETURN_GENERATED_KEYS)) {
                    personStatement.setString(1, agent.getName());
                    personStatement.setString(2, agent.getEmail());
                    personStatement.setString(3, agent.getPhone());
                    personStatement.executeUpdate();
                    try (ResultSet keys = personStatement.getGeneratedKeys()) {
                        if (keys.next()) {
                            agent.setId(keys.getLong(1));
                        } else {
                            throw new IllegalStateException("Unable to retrieve generated person identifier");
                        }
                    }
                }

                try (PreparedStatement agentStatement = connection.prepareStatement(insertAgent)) {
                    agentStatement.setLong(1, agent.getId());
                    agentStatement.setString(2, agent.getCode());
                    agentStatement.setString(3, agent.getRegion());
                    agentStatement.setString(4, agent.getStatus());
                    agentStatement.setTimestamp(5, Timestamp.valueOf(agent.getLastUpdate()));
                    agentStatement.executeUpdate();
                }

                connection.commit();
                return agent;
            } catch (Exception e) {
                connection.rollback();
                throw new IllegalStateException("Unable to save agent", e);
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save agent", e);
        }
    }

    @Override
    public Agent update(long id, Agent agent) {
        String updatePerson = "UPDATE persons SET full_name = ?, email = ?, phone = ? WHERE id = ?";
        String updateAgent = "UPDATE agents SET code = ?, region = ?, status = ?, last_update = ? WHERE person_id = ?";
        agent.setLastUpdate(LocalDateTime.now());
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement personStatement = connection.prepareStatement(updatePerson)) {
                    personStatement.setString(1, agent.getName());
                    personStatement.setString(2, agent.getEmail());
                    personStatement.setString(3, agent.getPhone());
                    personStatement.setLong(4, id);
                    personStatement.executeUpdate();
                }

                try (PreparedStatement agentStatement = connection.prepareStatement(updateAgent)) {
                    agentStatement.setString(1, agent.getCode());
                    agentStatement.setString(2, agent.getRegion());
                    agentStatement.setString(3, agent.getStatus());
                    agentStatement.setTimestamp(4, Timestamp.valueOf(agent.getLastUpdate()));
                    agentStatement.setLong(5, id);
                    agentStatement.executeUpdate();
                }

                connection.commit();
                agent.setId(id);
                return agent;
            } catch (Exception e) {
                connection.rollback();
                throw new IllegalStateException("Unable to update agent with id " + id, e);
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to update agent with id " + id, e);
        }
    }

    @Override
    public void delete(long id) {
        String sql = "DELETE FROM persons WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to delete agent with id " + id, e);
        }
    }

    private Agent mapRow(ResultSet rs) throws Exception {
        Agent agent = new Agent();
        agent.setId(rs.getLong("id"));
        agent.setName(rs.getString("name"));
        agent.setEmail(rs.getString("email"));
        agent.setPhone(rs.getString("phone"));
        agent.setCode(rs.getString("code"));
        agent.setRegion(rs.getString("region"));
        agent.setStatus(rs.getString("status"));
        Timestamp timestamp = rs.getTimestamp("last_update");
        agent.setLastUpdate(timestamp != null ? timestamp.toLocalDateTime() : null);
        return agent;
    }
}
