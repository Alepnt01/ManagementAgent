package com.managementagent.client;

import com.managementagent.client.controller.AgentController;
import com.managementagent.client.controller.CollaborationController;
import com.managementagent.client.controller.LoginController;
import com.managementagent.client.service.AgentApiClient;
import com.managementagent.client.view.AgentView;
import com.managementagent.client.view.EmailView;
import com.managementagent.client.view.LoginView;
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
        LoginController loginController = new LoginController(apiClient);

        AgentView agentView = new AgentView(agentController);
        TeamChatView chatView = new TeamChatView(collaborationController);
        EmailView emailView = new EmailView(collaborationController);
        MainView mainView = new MainView(agentView, chatView, emailView);
        Scene mainScene = new Scene(mainView.getRoot(), 1024, 720);

        LoginView loginView = new LoginView(loginController, () -> {
            stage.setScene(mainScene);
            stage.setResizable(true);
            stage.setTitle("Management Agent Console");
            stage.centerOnScreen();
            agentController.refreshAgents();
            collaborationController.refreshTeams();
            collaborationController.refreshEmployees();
            collaborationController.refreshClients();
        });
        Scene loginScene = new Scene(loginView.getRoot(), 420, 280);

        stage.setTitle("Accesso Management Agent");
        stage.setScene(loginScene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
