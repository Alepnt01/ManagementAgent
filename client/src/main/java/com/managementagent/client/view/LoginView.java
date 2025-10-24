package com.managementagent.client.view;

import com.managementagent.client.controller.LoginController;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.util.Objects;

/**
 * Simple login form displayed before accessing the management console.
 */
public class LoginView {

    private final BorderPane root = new BorderPane();
    private final TextField usernameField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final Button loginButton = new Button("Accedi");
    private final Label errorLabel = new Label();

    public LoginView(LoginController controller, Runnable onLoginSuccess) {
        Objects.requireNonNull(controller, "controller");
        Objects.requireNonNull(onLoginSuccess, "onLoginSuccess");

        Label title = new Label("Management Agent");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        usernameField.setPromptText("Username");
        passwordField.setPromptText("Password");
        errorLabel.setStyle("-fx-text-fill: red;");

        VBox form = new VBox(12, title, usernameField, passwordField, loginButton, errorLabel);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(32));

        root.setCenter(form);

        loginButton.setDefaultButton(true);
        passwordField.setOnAction(event -> loginButton.fire());

        loginButton.setOnAction(event -> {
            errorLabel.setText("");
            String username = usernameField.getText();
            String password = passwordField.getText();
            if (username == null || username.isBlank() || password == null || password.isBlank()) {
                errorLabel.setText("Inserire username e password");
                return;
            }
            loginButton.setDisable(true);
            controller.login(username.trim(), password)
                    .thenRun(() -> Platform.runLater(() -> {
                        loginButton.setDisable(false);
                        usernameField.clear();
                        passwordField.clear();
                        onLoginSuccess.run();
                    }))
                    .exceptionally(throwable -> {
                        Platform.runLater(() -> {
                            loginButton.setDisable(false);
                            errorLabel.setText("Credenziali non valide");
                        });
                        return null;
                    });
        });
    }

    public BorderPane getRoot() {
        return root;
    }
}
