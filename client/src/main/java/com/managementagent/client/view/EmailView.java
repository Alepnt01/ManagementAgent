package com.managementagent.client.view;

import com.managementagent.client.controller.CollaborationController;
import com.managementagent.client.model.ClientContact;
import com.managementagent.client.model.Employee;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.Optional;

public class EmailView {

    private final VBox root = new VBox(12);

    public EmailView(CollaborationController controller) {
        root.setPadding(new Insets(16));

        ComboBox<Employee> employeeComboBox = new ComboBox<>(controller.getEmployees());
        employeeComboBox.setPromptText("Mittente");

        ComboBox<ClientContact> clientComboBox = new ComboBox<>(controller.getClients());
        clientComboBox.setPromptText("Destinatario");

        TextField subjectField = new TextField();
        subjectField.setPromptText("Oggetto email");

        TextArea bodyArea = new TextArea();
        bodyArea.setPromptText("Testo dell'email");
        bodyArea.setWrapText(true);
        bodyArea.setPrefRowCount(10);

        GridPane form = new GridPane();
        form.setHgap(8);
        form.setVgap(8);

        form.add(new Label("Dipendente"), 0, 0);
        form.add(employeeComboBox, 1, 0);
        form.add(new Label("Cliente"), 0, 1);
        form.add(clientComboBox, 1, 1);
        form.add(new Label("Oggetto"), 0, 2);
        form.add(subjectField, 1, 2);
        form.add(new Label("Messaggio"), 0, 3);
        form.add(bodyArea, 1, 3);

        Button sendButton = new Button("Invia email");
        sendButton.setDefaultButton(true);
        sendButton.setOnAction(event -> {
            Employee employee = employeeComboBox.getSelectionModel().getSelectedItem();
            ClientContact client = clientComboBox.getSelectionModel().getSelectedItem();
            String subject = Optional.ofNullable(subjectField.getText()).map(String::trim).orElse("");
            String body = Optional.ofNullable(bodyArea.getText()).map(String::trim).orElse("");

            if (employee == null) {
                showAlert("Seleziona il dipendente mittente.");
                return;
            }
            if (client == null) {
                showAlert("Seleziona il cliente destinatario.");
                return;
            }
            if (subject.isBlank()) {
                showAlert("Inserisci l'oggetto dell'email.");
                return;
            }
            if (body.isBlank()) {
                showAlert("Inserisci il contenuto dell'email.");
                return;
            }

            sendButton.setDisable(true);
            controller.sendEmail(employee, client, subject, body)
                    .whenComplete((result, throwable) -> Platform.runLater(() -> {
                        sendButton.setDisable(false);
                        if (throwable != null) {
                            showAlert("Invio fallito: " + throwable.getMessage());
                        } else {
                            showAlert("Email inviata correttamente.");
                            subjectField.clear();
                            bodyArea.clear();
                        }
                    }));
        });

        root.getChildren().addAll(form, sendButton);
    }

    public VBox getRoot() {
        return root;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
