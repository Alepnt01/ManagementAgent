package com.managementagent.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.managementagent.server.controller.AgentController;
import com.managementagent.server.controller.CollaborationController;
import com.managementagent.server.controller.MailController;
import com.managementagent.server.dao.AgentDAO;
import com.managementagent.server.dao.CollaborationDAO;
import com.managementagent.server.dao.SqlServerAgentDAO;
import com.managementagent.server.dao.SqlServerCollaborationDAO;
import com.managementagent.server.factory.AgentFactory;
import com.managementagent.server.observer.AgentEventPublisher;
import com.managementagent.server.service.AgentService;
import com.managementagent.server.service.MailService;
import com.managementagent.server.service.TeamService;
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
        private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        private final AgentFactory agentFactory = new AgentFactory();
        private final AgentEventPublisher eventPublisher = new AgentEventPublisher();
        private final com.managementagent.server.observer.LoggingAgentListener loggingListener =
                new com.managementagent.server.observer.LoggingAgentListener();
        private final AgentDAO agentDAO = new SqlServerAgentDAO();
        private final CollaborationDAO collaborationDAO = new SqlServerCollaborationDAO();
        private final AgentService agentService = new AgentService(agentDAO, agentFactory, eventPublisher);
        private final TeamService teamService = new TeamService(collaborationDAO);
        private final MailService mailService = new MailService(collaborationDAO);
        private final AgentController agentController = new AgentController(agentService, objectMapper);
        private final CollaborationController collaborationController = new CollaborationController(teamService, objectMapper);
        private final MailController mailController = new MailController(mailService, objectMapper);

        public void start() {
            eventPublisher.register(loggingListener);
            int port = ServerSettings.getPort();
            Javalin app = Javalin.create(config -> config.plugins.enableCors(cors -> cors.add(it -> it.anyHost())));

            agentController.registerRoutes(app);
            collaborationController.registerRoutes(app);
            mailController.registerRoutes(app);

            app.events(event -> event.serverStopped(() -> {
                agentService.shutdown();
                teamService.shutdown();
                mailService.shutdown();
            }));

            app.start(port);
        }
    }
}
