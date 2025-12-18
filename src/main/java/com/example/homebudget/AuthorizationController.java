package com.example.homebudget;

import java.io.IOException;
import java.net.URL;
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

public class AuthorizationController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField login_field;

    @FXML
    private PasswordField password_field;

    @FXML
    private Button regTextButton;

    @FXML
    private Button сontinueButton;

    private static User currentUser;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    private boolean validateUser(String login, String password) {
        DatabaseHandler dbHandler = new DatabaseHandler();
        try {
            User user = dbHandler.getUser(login, password);
            if (user != null) {
                System.out.println("[DEBUG] Пользователь найден в базе данных: " + user.getUserName());
                // Сохраняем текущего пользователя
                AuthorizationController.setCurrentUser(user);
                return true;
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Ошибка при проверке пользователя: " + e.getMessage());
        }
        return false;
    }

    @FXML
    void initialize() {
        сontinueButton.setOnAction(actionEvent -> {
            String loginText = login_field.getText().trim();
            String passwordText = password_field.getText().trim();

            // Проверка заполнения полей
            if (loginText.isEmpty() || passwordText.isEmpty()) {
                System.out.println("[VALIDATION] Ошибка: поля логина и пароля обязательны");
                return;
            }

            // Проверка учетных данных
            if (validateUser(loginText, passwordText)) {
                System.out.println("[AUTH] Успешная авторизация для пользователя: " + loginText);
                try {
                    // Закрытие текущего окна
                    Stage currentStage = (Stage) сontinueButton.getScene().getWindow();
                    currentStage.close();

                    // Открытие главного меню
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("main-menu.fxml"));
                    Parent root = loader.load();
                    Stage newStage = new Stage();
                    newStage.setScene(new Scene(root));
                    newStage.show();
                } catch (IOException e) {
                    System.err.println("[ERROR] Ошибка при загрузке main-menu.fxml: " + e.getMessage());
                }
            } else {
                System.out.println("[AUTH] Ошибка: неверный логин или пароль");
            }
        });

        regTextButton.setOnAction(actionEvent -> {
            System.out.println("[NAVIGATION] Переход к регистрации");
            try {
                Stage currentStage = (Stage) regTextButton.getScene().getWindow();
                currentStage.close();

                Parent root = FXMLLoader.load(getClass().getResource("registration.fxml"));
                Stage newStage = new Stage();
                newStage.setScene(new Scene(root));
                newStage.show();
            } catch (IOException e) {
                System.err.println("[ERROR] Ошибка при загрузке registration.fxml: " + e.getMessage());
            }
        });
    }
}