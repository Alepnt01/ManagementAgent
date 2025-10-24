package com.managementagent.server.observer;

import com.managementagent.server.model.Agent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple listener logging events. Useful to extend the observer pipeline.
 */
public class LoggingAgentListener implements AgentEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingAgentListener.class);

    @Override
    public void onAgentCreated(Agent agent) {
        LOGGER.info("Agent created: {}", agent.getCode());
    }

    @Override
    public void onAgentUpdated(Agent agent) {
        LOGGER.info("Agent updated: {}", agent.getCode());
    }

    @Override
    public void onAgentDeleted(long agentId) {
        LOGGER.info("Agent deleted: {}", agentId);
    }
}
