package org.example.tasktraker.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.tasktraker.network.NetworkClient;
import org.example.tasktraker.network.Request;
import org.example.tasktraker.network.Response;

public class RegisterController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML private ComboBox<String> roleComboBox;

    @FXML private Button registerButton;
    @FXML private Hyperlink loginLink;
    @FXML private Label errorLabel;

    public void initialize() {
        roleComboBox.getItems().addAll("DEVELOPER", "TESTER");

        registerButton.setOnAction(e -> handleRegister());
        loginLink.setOnAction(e -> openLogin());
    }

    private void handleRegister() {
        String name = nameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String role = roleComboBox.getValue();

        try {
            String[] payload = {name, email, password, confirmPassword, role};
            Response response = NetworkClient.getInstance().sendRequest(new Request("REGISTER", payload));

            if (response != null && response.isSuccess()) {
                openLogin();
            } else {
                showError(response != null ? response.getMessage() : "Server is not responding");
            }

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void openLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/tasktraker/login_screen.fxml"));
            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) registerButton.getScene().getWindow();
            stage.setScene(scene);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
