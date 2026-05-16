package org.example.tasktraker.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.tasktraker.DeveloperController;
import org.example.tasktraker.TesterController;
import org.example.tasktraker.entity.User;
import org.example.tasktraker.service.UserService;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;
    @FXML private Hyperlink registerLink;

    private final UserService userService = new UserService();

    public void initialize() {
        loginButton.setOnAction(e -> handleLogin());
        registerLink.setOnAction(e -> openRegister());
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Fill all fields");
            return;
        }

        try {
            User user = userService.login(email, password);

            if (user != null) {
                openDashboard(user.getRole(), user.getId());
            } else {
                showError("Invalid email or password");
            }

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void openDashboard(String role, int userId) {
        try {
            String fxmlFile;

            switch (role) {
                case "ADMIN":
                    fxmlFile = "/org/example/tasktraker/admin_screen.fxml";
                    break;
                case "DEVELOPER":
                    fxmlFile = "/org/example/tasktraker/developer_screen.fxml";
                    break;
                case "TESTER":
                    fxmlFile = "/org/example/tasktraker/tester_screen.fxml";
                    break;
                default:
                    showError("Unknown role");
                    return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Scene scene = new Scene(loader.load());

            Object controller = loader.getController();

            if (controller instanceof DeveloperController) {
                //((DeveloperController) controller).setUserId(userId);
            }
            if (controller instanceof TesterController) {
                //((TesterController) controller).setUserId(userId);
            }
            if (controller instanceof AdminController) {
                ((AdminController) controller).setUserId(userId);
            }

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);

        } catch (Exception e) {
            showError("Error loading screen");
        }
    }

    private void openRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/tasktraker/register_screen.fxml"));
            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);

        } catch (Exception e) {
            showError("Cannot open register");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}