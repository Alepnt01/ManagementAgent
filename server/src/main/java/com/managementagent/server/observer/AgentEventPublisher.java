package com.managementagent.server.observer;

import com.managementagent.server.model.Agent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Basic publisher implementing the Observer pattern for audit or logging extensions.
 */
public class AgentEventPublisher {

    private final List<AgentEventListener> listeners = new CopyOnWriteArrayList<>();

    public void register(AgentEventListener listener) {
        listeners.add(listener);
    }

    public void unregister(AgentEventListener listener) {
        listeners.remove(listener);
    }

    public void publishAgentCreated(Agent agent) {
        listeners.forEach(listener -> listener.onAgentCreated(agent));
    }

    public void publishAgentUpdated(Agent agent) {
        listeners.forEach(listener -> listener.onAgentUpdated(agent));
    }

    public void publishAgentDeleted(long agentId) {
        listeners.forEach(listener -> listener.onAgentDeleted(agentId));
    }
}
