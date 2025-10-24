package com.managementagent.client.view;

import com.managementagent.client.controller.CollaborationController;
import com.managementagent.client.model.ChatMessage;
import com.managementagent.client.model.Employee;
import com.managementagent.client.model.Team;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.util.Optional;

/**
 * Vista JavaFX per gestire la chat tra membri dello stesso team.
 */
public class TeamChatView {

    private final BorderPane root = new BorderPane();
    private final ComboBox<Team> teamComboBox = new ComboBox<>();
    private final ComboBox<Employee> employeeComboBox = new ComboBox<>();
    private final ListView<ChatMessage> messageListView = new ListView<>();
    private final TextArea messageArea = new TextArea();

    public TeamChatView(CollaborationController controller) {
        // Configura l'interfaccia grafica e la collega al controller di collaborazione.
        root.setPadding(new Insets(16));

        GridPane header = new GridPane();
        header.setHgap(8);
        header.setVgap(8);

        // ComboBox per scegliere il team su cui operare.
        teamComboBox.setPromptText("Seleziona team");
        teamComboBox.setItems(controller.getTeams());
        // ComboBox con i membri del team selezionato.
        employeeComboBox.setPromptText("Mittente");
        employeeComboBox.setItems(controller.getEmployees());

        header.add(new Label("Team"), 0, 0);
        header.add(teamComboBox, 1, 0);
        header.add(new Label("Dipendente"), 0, 1);
        header.add(employeeComboBox, 1, 1);

        Button refreshButton = new Button("Ricarica chat");
        header.add(refreshButton, 2, 0, 1, 2);
        // Permette di ricaricare manualmente i messaggi della chat corrente.
        refreshButton.setOnAction(event -> {
            Team selected = teamComboBox.getSelectionModel().getSelectedItem();
            if (selected != null) {
                controller.refreshMessages(selected.getId());
            }
        });

        root.setTop(header);

        messageListView.setItems(controller.getMessages());
        // Mostra ciascun messaggio convertendolo in stringa.
        messageListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(ChatMessage item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
            }
        });
        root.setCenter(messageListView);

        messageArea.setPromptText("Scrivi un messaggio al team...");
        messageArea.setWrapText(true);

        Button sendButton = new Button("Invia");
        // Gestisce l'invio asincrono dei messaggi verso il server REST.
        sendButton.setOnAction(event -> {
            Team team = teamComboBox.getSelectionModel().getSelectedItem();
            Employee employee = employeeComboBox.getSelectionModel().getSelectedItem();
            String text = Optional.ofNullable(messageArea.getText()).map(String::trim).orElse("");
            if (team == null) {
                showAlert("Seleziona un team prima di inviare un messaggio.");
                return;
            }
            if (employee == null) {
                showAlert("Seleziona il dipendente mittente.");
                return;
            }
            if (text.isBlank()) {
                showAlert("Il messaggio non può essere vuoto.");
                return;
            }
            sendButton.setDisable(true);
            controller.sendChatMessage(team, employee, text)
                    .whenComplete((result, throwable) -> Platform.runLater(() -> {
                        sendButton.setDisable(false);
                        if (throwable != null) {
                            showAlert("Invio non riuscito: " + throwable.getMessage());
                        } else {
                            messageArea.clear();
                        }
                    }));
        });

        HBox composer = new HBox(8, messageArea, sendButton);
        composer.setPadding(new Insets(12, 0, 0, 0));
        messageArea.setPrefRowCount(3);
        messageArea.setPrefColumnCount(40);
        root.setBottom(composer);

        // Aggiorna dinamicamente i membri del team quando cambia la selezione.
        teamComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldTeam, newTeam) -> {
            if (newTeam != null) {
                employeeComboBox.setItems(newTeam.getMembers());
                controller.refreshMessages(newTeam.getId());
            } else {
                employeeComboBox.setItems(controller.getEmployees());
                controller.getMessages().clear();
            }
        });
    }

    public BorderPane getRoot() {
        // Restituisce il nodo principale da incorporare nelle scene JavaFX.
        return root;
    }

    private void showAlert(String message) {
        // Mostra un semplice popup informativo con il messaggio fornito.
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
