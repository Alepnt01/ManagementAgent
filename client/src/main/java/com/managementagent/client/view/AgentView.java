package com.managementagent.client.view;

import com.managementagent.client.controller.AgentController;
import com.managementagent.client.model.Agent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * JavaFX view that renders the management console.
 */
public class AgentView {

    private final BorderPane root = new BorderPane();
    private final TableView<Agent> tableView = new TableView<>();
    private final TextField codeField = new TextField();
    private final TextField nameField = new TextField();
    private final TextField regionField = new TextField();
    private final ComboBox<String> statusBox = new ComboBox<>();

    public AgentView(AgentController controller) {
        createTable(controller);
        createForm(controller);
    }

    public BorderPane getRoot() {
        return root;
    }

    private void createTable(AgentController controller) {
        TableColumn<Agent, Number> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());

        TableColumn<Agent, String> codeColumn = new TableColumn<>("Codice");
        codeColumn.setCellValueFactory(cellData -> cellData.getValue().codeProperty());

        TableColumn<Agent, String> nameColumn = new TableColumn<>("Nome");
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());

        TableColumn<Agent, String> regionColumn = new TableColumn<>("Regione");
        regionColumn.setCellValueFactory(cellData -> cellData.getValue().regionProperty());

        TableColumn<Agent, String> statusColumn = new TableColumn<>("Stato");
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        tableView.getColumns().addAll(idColumn, codeColumn, nameColumn, regionColumn, statusColumn);
        tableView.setItems(controller.getAgents());

        root.setCenter(tableView);
    }

    private void createForm(AgentController controller) {
        GridPane form = new GridPane();
        form.setPadding(new Insets(16));
        form.setHgap(8);
        form.setVgap(8);

        statusBox.getItems().addAll("ATTIVO", "SOSPESO", "INATTIVO");

        form.add(new Label("Codice"), 0, 0);
        form.add(codeField, 1, 0);
        form.add(new Label("Nome"), 0, 1);
        form.add(nameField, 1, 1);
        form.add(new Label("Regione"), 0, 2);
        form.add(regionField, 1, 2);
        form.add(new Label("Stato"), 0, 3);
        form.add(statusBox, 1, 3);

        HBox buttons = new HBox(8);
        Button addButton = new Button("Aggiungi");
        Button updateButton = new Button("Aggiorna");
        Button deleteButton = new Button("Elimina");
        Button refreshButton = new Button("Ricarica");
        buttons.getChildren().addAll(addButton, updateButton, deleteButton, refreshButton);

        HBox.setHgrow(addButton, Priority.ALWAYS);

        addButton.setOnAction(event -> controller.createAgent(
                codeField.getText(),
                nameField.getText(),
                regionField.getText(),
                statusBox.getSelectionModel().getSelectedItem()
        ));

        updateButton.setOnAction(event -> {
            Agent selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                controller.updateAgent(selected);
            }
        });

        deleteButton.setOnAction(event -> {
            Agent selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                controller.deleteAgent(selected);
            }
        });

        refreshButton.setOnAction(event -> controller.refreshAgents());

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                codeField.setText(newVal.getCode());
                nameField.setText(newVal.getName());
                regionField.setText(newVal.getRegion());
                statusBox.getSelectionModel().select(newVal.getStatus());
            }
        });

        form.add(buttons, 0, 4, 2, 1);
        root.setBottom(form);
    }
}
