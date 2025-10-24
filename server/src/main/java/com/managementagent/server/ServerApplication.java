package com.managementagent.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.managementagent.server.controller.AgentController;
import com.managementagent.server.dao.AgentDAO;
import com.managementagent.server.dao.SqlServerAgentDAO;
import com.managementagent.server.factory.AgentFactory;
import com.managementagent.server.observer.AgentEventPublisher;
import com.managementagent.server.service.AgentService;
import io.javalin.Javalin;

/**
 * Entry point for the Management Agent server. It wires together the
 * different components following an object-oriented approach without
 * relying on dependency injection frameworks.
 */
public class ServerApplication {

    public static void main(String[] args) {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.start();
    }

    private static class ServerBootstrap {
        private final ObjectMapper objectMapper = new ObjectMapper();
        private final AgentFactory agentFactory = new AgentFactory();
        private final AgentEventPublisher eventPublisher = new AgentEventPublisher();
        private final com.managementagent.server.observer.LoggingAgentListener loggingListener =
                new com.managementagent.server.observer.LoggingAgentListener();
        private final AgentDAO agentDAO = new SqlServerAgentDAO();
        private final AgentService agentService = new AgentService(agentDAO, agentFactory, eventPublisher);
        private final AgentController agentController = new AgentController(agentService, objectMapper);

        public void start() {
            eventPublisher.register(loggingListener);
            int port = ServerSettings.getPort();
            Javalin app = Javalin.create(config -> config.plugins.enableCors(cors -> cors.add(it -> it.anyHost())));

            agentController.registerRoutes(app);

            app.events(event -> event.serverStopped(agentService::shutdown));

            app.start(port);
        }
    }
}
