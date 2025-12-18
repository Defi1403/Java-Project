package com.example.homebudget;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class ChangeExpenseController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML private TextField amountTextField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private DatePicker dateDatePicker;
    @FXML private Button saveButton;
    @FXML private ImageView returnImage;

    private final DatabaseHandler dbHandler = new DatabaseHandler();

    private void loadCategories() {
        try {
            List<String> categories = dbHandler.getCategories();
            categoryComboBox.setItems(FXCollections.observableArrayList(categories));
        } catch (Exception e) {
            System.err.println("[ERROR] Ошибка загрузки категорий: " + e.getMessage());
        }
    }

    @FXML
    void initialize() {
        dateDatePicker.setValue(LocalDate.now());
        loadCategories();

        returnImage.setOnMouseClicked(actionEvent -> {
            returnImage.getScene().getWindow().hide();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("listExpense.fxml"));
            try {
                loader.load();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Parent root = loader.getRoot();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        });
        saveButton.setOnAction(actionEvent -> {
            BigDecimal amount;
            String category = categoryComboBox.getValue();
            LocalDate date = dateDatePicker.getValue();

            try {
                // Создаем BigDecimal непосредственно из строки
                amount = new BigDecimal(amountTextField.getText().trim());

                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    System.out.println("Ошибка! Сумма должна быть больше нуля.");
                    return;
                }
            } catch (NumberFormatException | NullPointerException ex) {
                System.out.println("Ошибка! Введена недопустимая сумма.");
                return;
            }

            if (category == null || category.isEmpty()) {
                System.out.println("Ошибка, выберите категорию");
                return;
            }

            if (date == null) {
                System.out.println("Ошибка, выберите дату");
                return;
            }

            int userId = AuthorizationController.getCurrentUser().getId();
            dbHandler.addExpense(userId, amount, category, Date.valueOf(date));

            System.out.println("[EXPENSE] Доход успешно Изменен");
            saveButton.getScene().getWindow().hide();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("listExpense  .fxml"));
            try {
                loader.load();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Parent root = loader.getRoot();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        });
    }

}
