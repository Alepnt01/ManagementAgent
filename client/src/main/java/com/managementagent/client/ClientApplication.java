package com.managementagent.client;

import com.managementagent.client.controller.AgentController;
import com.managementagent.client.service.AgentApiClient;
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
        AgentController controller = new AgentController(apiClient);
        var view = new com.managementagent.client.view.AgentView(controller);

        stage.setTitle("Management Agent Console");
        stage.setScene(new Scene(view.getRoot(), 800, 600));
        stage.show();

        controller.refreshAgents();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
