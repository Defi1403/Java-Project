package com.example.homebudget;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

public class ChangeCategoryController {

    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private TextField nameTextField;
    @FXML private Button saveButton;
    @FXML private ImageView returnImage;

    private boolean categoryExists(String categoryName) {
        String query = "SELECT * FROM Categories WHERE category_name = ?";

        try (Connection connection = new DatabaseHandler().getDbConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, categoryName);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("[DATABASE] Ошибка при проверке категории: " + e.getMessage());
        }
        return false;
    }

    private void switchToCategoryScene() {
        try {
            Stage currentStage = (Stage) saveButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("listCategory.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
            currentStage.close();
        } catch (IOException e) {
            System.err.println("[ERROR] Ошибка при загрузке сцены: " + e.getMessage());
        }
    }

    @FXML
    void initialize() {
        returnImage.setOnMouseClicked(event -> switchToCategoryScene());

        saveButton.setOnAction(event -> {
            String categoryName = nameTextField.getText().trim();

            if (categoryName.isEmpty()) {
                System.out.println("[VALIDATION] Ошибка: название категории не может быть пустым");
                return;
            }

            if (categoryExists(categoryName)) {
                System.out.println("[VALIDATION] Ошибка: категория уже существует");
                return;
            }

            DatabaseHandler dbHandler = new DatabaseHandler();
            dbHandler.addCategory(categoryName);
            System.out.println("[CATEGORY] Категория '" + categoryName + "' успешно добавлена");
            switchToCategoryScene();

        });
    }
}