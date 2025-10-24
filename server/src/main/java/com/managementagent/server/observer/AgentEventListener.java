package com.managementagent.server.observer;

import com.managementagent.server.model.Agent;

/**
 * Observer contract for agent events.
 */
public interface AgentEventListener {

    void onAgentCreated(Agent agent);

    void onAgentUpdated(Agent agent);

    void onAgentDeleted(long agentId);
}
