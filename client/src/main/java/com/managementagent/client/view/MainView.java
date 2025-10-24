package com.managementagent.client.view;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class MainView {

    private final TabPane root = new TabPane();

    public MainView(AgentView agentView, TeamChatView teamChatView, EmailView emailView) {
        Tab agentsTab = new Tab("Agenti", agentView.getRoot());
        agentsTab.setClosable(false);

        Tab chatTab = new Tab("Chat Team", teamChatView.getRoot());
        chatTab.setClosable(false);

        Tab emailTab = new Tab("Email Clienti", emailView.getRoot());
        emailTab.setClosable(false);

        root.getTabs().addAll(agentsTab, chatTab, emailTab);
    }

    public TabPane getRoot() {
        return root;
    }
}
