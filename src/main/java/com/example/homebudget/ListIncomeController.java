package com.example.homebudget;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;

public class ListIncomeController {

    @FXML private TableView<IncomeRecord> listIncomeTableView;
    @FXML private TableColumn<IncomeRecord, Date> dateTableColumn;
    @FXML private TableColumn<IncomeRecord, String> categoryTableColumn;
    @FXML private TableColumn<IncomeRecord, BigDecimal> amountTableColumn;
    @FXML private Button changeButton;
    @FXML private Button deleteButton;
    @FXML private ImageView returnImage;
    @FXML private Button saveButton;

    private IncomeRecord selectedIncome;
    private final DatabaseHandler dbHandler = new DatabaseHandler();

    @FXML
    void initialize() {
        // Инициализация таблицы
        dateTableColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        categoryTableColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        amountTableColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));

        // Загрузка данных
        loadIncomes();

        // Выбор записи
        listIncomeTableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> selectedIncome = newSelection);

        // Кнопка удаления
        deleteButton.setOnAction(event -> {
            if (selectedIncome != null) {
                try {
                    dbHandler.deleteIncome(selectedIncome.getId());
                    loadIncomes();
                    System.out.println("[INCOME] Запись удалена");
                } catch (Exception e) {
                    System.err.println("[ERROR] Ошибка удаления: " + e.getMessage());
                }
            }
        });

        // Кнопка изменения
        changeButton.setOnAction(event -> {
            if (selectedIncome != null) {
                try {
                    // Удаляем выбранную запись
                    dbHandler.deleteIncome(selectedIncome.getId());

                    changeButton.getScene().getWindow().hide();
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(getClass().getResource("changeIncome.fxml"));
                    try {
                        loader.load();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    Parent root = loader.getRoot();
                    Stage stage = new Stage();
                    stage.setScene(new Scene(root));
                    stage.show();

                } catch (Exception e) {
                    System.err.println("[ERROR] Ошибка при изменении записи: " + e.getMessage());
                }
            }
        });

        // Возврат
        returnImage.setOnMouseClicked(event -> switchToIncomeScene());

        saveButton.setOnAction(actionEvent -> {
            saveButton.getScene().getWindow().hide();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("income.fxml"));
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

    private void loadIncomes() {
        try {
            int userId = AuthorizationController.getCurrentUser().getId();
            ObservableList<IncomeRecord> incomes = dbHandler.getUserIncomes(userId);
            listIncomeTableView.setItems(incomes);
        } catch (Exception e) {
            System.err.println("[ERROR] Ошибка загрузки доходов: " + e.getMessage());
        }
    }

    private void switchToIncomeScene() {
        try {
            Stage currentStage = (Stage) returnImage.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("income.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
            currentStage.close();
        } catch (IOException e) {
            System.err.println("[ERROR] Ошибка перехода: " + e.getMessage());
        }
    }
}