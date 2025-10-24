package com.managementagent.server.dao;

import com.managementagent.server.model.Agent;

import java.util.List;
import java.util.Optional;

/**
 * DAO pattern abstraction for the Agent entity.
 */
public interface AgentDAO {

    List<Agent> findAll();

    Optional<Agent> findById(long id);

    Agent save(Agent agent);

    Agent update(long id, Agent agent);

    void delete(long id);
}
