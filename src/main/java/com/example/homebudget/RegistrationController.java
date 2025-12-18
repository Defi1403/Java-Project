package com.example.homebudget;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegistrationController {

    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private TextField email_field;
    @FXML private TextField login_field;
    @FXML private TextField name_field;
    @FXML private PasswordField password_field;
    @FXML private Button authTextButton;
    @FXML private Button сontinueButton;

    private boolean isUserExists(String login, String email) {
        String query = "SELECT * FROM Users WHERE login = ? OR email = ?";

        try (Connection connection = new DatabaseHandler().getDbConnection();
             PreparedStatement prSt = connection.prepareStatement(query)) {

            prSt.setString(1, login);
            prSt.setString(2, email);

            try (ResultSet result = prSt.executeQuery()) {
                return result.next(); // Если есть результаты - пользователь существует
            }
        } catch (Exception e) {
            System.err.println("[DATABASE] Ошибка при проверке пользователя: " + e.getMessage());
            return false;
        }
    }

    private boolean validateInput(String name, String login, String password, String email) {
        // Проверка на пустые поля
        if (name.isEmpty() || login.isEmpty() || password.isEmpty() || email.isEmpty()) {
            System.out.println("[VALIDATION] Ошибка: все поля обязательны для заполнения");
            return false;
        }

        // Проверка длины пароля
        if (password.length() < 6) {
            System.out.println("[VALIDATION] Ошибка: пароль должен содержать минимум 6 символов");
            return false;
        }

        // Проверка формата email
        if (!email.contains("@")) {
            System.out.println("[VALIDATION] Ошибка: email должен содержать символ @");
            return false;
        }

        return true;
    }

    private boolean regNewUser() {
        String name = name_field.getText().trim();
        String login = login_field.getText().trim();
        String password = password_field.getText().trim();
        String email = email_field.getText().trim();

        // Валидация ввода
        if (!validateInput(name, login, password, email)) {
            return false;
        }

        // Проверка на существующего пользователя
        if (isUserExists(login, email)) {
            System.out.println("[REG] Пользователь с таким логином или почтой уже зарегистрирован");
            return false;
        }

        DatabaseHandler dbHandler = new DatabaseHandler();
        User user = new User(0, name, login, password, email);

        int userId = dbHandler.regUser(user);

        if (userId != -1) {
            user.setId(userId);
            AuthorizationController.setCurrentUser(user);
            return true;
        }
        return false;
    }

    @FXML
    void initialize() {
        сontinueButton.setOnAction(actionEvent -> {
            if (regNewUser()) {
                System.out.println("[REG] Успешная регистрация пользователя: " + login_field.getText().trim());
                try {
                    Stage currentStage = (Stage) сontinueButton.getScene().getWindow();
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("main-menu.fxml"));
                    Parent root = loader.load();
                    Stage newStage = new Stage();
                    newStage.setScene(new Scene(root));
                    newStage.show();
                    currentStage.close();
                } catch (IOException e) {
                    System.err.println("[ERROR] Ошибка при загрузке main-menu.fxml: " + e.getMessage());
                }
            }
        });

        authTextButton.setOnAction(actionEvent -> {
            System.out.println("[NAVIGATION] Переход к авторизации");
            try {
                Stage currentStage = (Stage) authTextButton.getScene().getWindow();
                currentStage.close();
                Parent root = FXMLLoader.load(getClass().getResource("authorization.fxml"));
                Stage newStage = new Stage();
                newStage.setScene(new Scene(root));
                newStage.show();
            } catch (IOException e) {
                System.err.println("[ERROR] Ошибка при загрузке authorization.fxml: " + e.getMessage());
            }
        });
    }
}