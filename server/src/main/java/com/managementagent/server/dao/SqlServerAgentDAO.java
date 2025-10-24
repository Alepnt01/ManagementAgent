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
        String sql = "SELECT id, code, name, region, status, last_update FROM agents ORDER BY id";
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
        String sql = "SELECT id, code, name, region, status, last_update FROM agents WHERE id = ?";
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
        String sql = "INSERT INTO agents (code, name, region, status, last_update) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, agent.getCode());
            statement.setString(2, agent.getName());
            statement.setString(3, agent.getRegion());
            statement.setString(4, agent.getStatus());
            statement.setTimestamp(5, Timestamp.valueOf(agent.getLastUpdate()));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    agent.setId(keys.getLong(1));
                }
            }
            return agent;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save agent", e);
        }
    }

    @Override
    public Agent update(long id, Agent agent) {
        String sql = "UPDATE agents SET code = ?, name = ?, region = ?, status = ?, last_update = ? WHERE id = ?";
        agent.setLastUpdate(LocalDateTime.now());
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, agent.getCode());
            statement.setString(2, agent.getName());
            statement.setString(3, agent.getRegion());
            statement.setString(4, agent.getStatus());
            statement.setTimestamp(5, Timestamp.valueOf(agent.getLastUpdate()));
            statement.setLong(6, id);
            statement.executeUpdate();
            agent.setId(id);
            return agent;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to update agent with id " + id, e);
        }
    }

    @Override
    public void delete(long id) {
        String sql = "DELETE FROM agents WHERE id = ?";
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
        agent.setCode(rs.getString("code"));
        agent.setName(rs.getString("name"));
        agent.setRegion(rs.getString("region"));
        agent.setStatus(rs.getString("status"));
        Timestamp timestamp = rs.getTimestamp("last_update");
        agent.setLastUpdate(timestamp != null ? timestamp.toLocalDateTime() : null);
        return agent;
    }
}
