package com.managementagent.client;

import com.managementagent.client.controller.AgentController;
import com.managementagent.client.controller.CollaborationController;
import com.managementagent.client.service.AgentApiClient;
import com.managementagent.client.view.AgentView;
import com.managementagent.client.view.EmailView;
import com.managementagent.client.view.MainView;
import com.managementagent.client.view.TeamChatView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main JavaFX application entry point.
 */
public class ClientApplication extends Application {

    @Override
    public void start(Stage stage) {
        String baseUrl = System.getProperty("management.agent.api", "http://localhost:7070");
        AgentApiClient apiClient = new AgentApiClient(baseUrl);
        AgentController agentController = new AgentController(apiClient);
        CollaborationController collaborationController = new CollaborationController(apiClient);

        AgentView agentView = new AgentView(agentController);
        TeamChatView chatView = new TeamChatView(collaborationController);
        EmailView emailView = new EmailView(collaborationController);
        MainView mainView = new MainView(agentView, chatView, emailView);

        stage.setTitle("Management Agent Console");
        stage.setScene(new Scene(mainView.getRoot(), 1024, 720));
        stage.show();

        agentController.refreshAgents();
        collaborationController.refreshTeams();
        collaborationController.refreshEmployees();
        collaborationController.refreshClients();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
