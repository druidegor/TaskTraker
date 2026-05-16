package org.example.tasktraker.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.tasktraker.DeveloperController;
import org.example.tasktraker.entity.User;
import org.example.tasktraker.network.NetworkClient;
import org.example.tasktraker.network.Request;
import org.example.tasktraker.network.Response;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;
    @FXML private Hyperlink registerLink;

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
            // 1. Упаковываем логин и пароль в массив
            String[] credentials = {email, password};

            // 2. Создаем письмо (Запрос) для сервера
            Request request = new Request("LOGIN", credentials);

            // 3. Отправляем письмо через наш Singleton-посредник и ждем ответ
            Response response = NetworkClient.getInstance().sendRequest(request);

            // 4. Проверяем, что ответил сервер
            if (response != null && response.isSuccess()) {
                // Сервер пустил нас! Достаем объект пользователя
                User user = (User) response.getData();
                openDashboard(user.getRole(), user.getId());
            } else {
                // Сервер не пустил (неверный пароль или логин)
                showError(response != null ? response.getMessage() : "Нет ответа от сервера");
            }

        } catch (Exception e) {
            showError("Ошибка: " + e.getMessage());
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

            if (controller instanceof AdminController) {
                ((AdminController) controller).setUserId(userId);
            }
            if (controller instanceof TesterController) {
                ((TesterController) controller).setUserId(userId);
            }

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);

        } catch (Exception e) {
            e.printStackTrace();
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
