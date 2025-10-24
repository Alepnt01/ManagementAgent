package com.managementagent.server.factory;

import com.managementagent.server.model.Agent;
import com.managementagent.server.model.AgentRequest;

import java.time.LocalDateTime;

/**
 * Demonstrates the Factory Method pattern by centralising the creation of
 * {@link Agent} instances.
 */
public class AgentFactory {

    public Agent createAgent(AgentRequest request) {
        Agent agent = new Agent();
        agent.setCode(request.getCode());
        agent.setName(request.getName());
        agent.setRegion(request.getRegion());
        agent.setStatus(request.getStatus());
        agent.setLastUpdate(LocalDateTime.now());
        return agent;
    }
}
