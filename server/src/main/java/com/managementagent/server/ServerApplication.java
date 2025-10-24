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
 * Punto di ingresso per il server Management Agent.
 * Collega tra loro i diversi componenti seguendo un approccio completamente ad oggetti
 * senza dipendere da framework di iniezione delle dipendenze.
 */
public class ServerApplication {

    public static void main(String[] args) {
        // Avvio esplicito del bootstrap per inizializzare ed esporre il server REST.
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.start();
    }

    private static class ServerBootstrap {
        // Mapper JSON configurato con il supporto per le date del pacchetto java.time.
        private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        // Factory responsabile della creazione delle entità Agent lato server.
        private final AgentFactory agentFactory = new AgentFactory();
        // Publisher per il pattern Observer che notifica le variazioni sugli agenti.
        private final AgentEventPublisher eventPublisher = new AgentEventPublisher();
        // Listener di logging che riceve gli eventi emessi dal publisher.
        private final com.managementagent.server.observer.LoggingAgentListener loggingListener =
                new com.managementagent.server.observer.LoggingAgentListener();
        // DAO concreto per la persistenza degli agenti su SQL Server.
        private final AgentDAO agentDAO = new SqlServerAgentDAO();
        // DAO concreto per le funzionalità di collaborazione (team, chat, email).
        private final CollaborationDAO collaborationDAO = new SqlServerCollaborationDAO();
        // Servizio di dominio per le operazioni CRUD sugli agenti.
        private final AgentService agentService = new AgentService(agentDAO, agentFactory, eventPublisher);
        // Servizio dedicato alle operazioni di team e chat.
        private final TeamService teamService = new TeamService(collaborationDAO);
        // Servizio che orchestra l'invio delle email ai clienti.
        private final MailService mailService = new MailService(collaborationDAO);
        // Controller REST per la gestione degli agenti.
        private final AgentController agentController = new AgentController(agentService, objectMapper);
        // Controller REST per chat e collaborazione tra dipendenti.
        private final CollaborationController collaborationController = new CollaborationController(teamService, objectMapper);
        // Controller REST per la spedizione delle email ai clienti.
        private final MailController mailController = new MailController(mailService, objectMapper);

        public void start() {
            // Registro il listener di logging in modo che riceva tutte le notifiche sugli agenti.
            eventPublisher.register(loggingListener);
            // Recupero la porta dalle impostazioni centralizzate del server.
            int port = ServerSettings.getPort();
            // Creo l'istanza di Javalin abilitando il CORS verso qualsiasi origine per il client JavaFX.
            Javalin app = Javalin.create(config -> config.plugins.enableCors(cors -> cors.add(it -> it.anyHost())));

            // Registro le rotte REST di ciascun controller.
            agentController.registerRoutes(app);
            collaborationController.registerRoutes(app);
            mailController.registerRoutes(app);

            // Quando il server si ferma rilascio i thread pool dei servizi per evitare leak.
            app.events(event -> event.serverStopped(() -> {
                agentService.shutdown();
                teamService.shutdown();
                mailService.shutdown();
            }));

            // Avvio il server HTTP sulla porta configurata.
            app.start(port);
        }
    }
}
