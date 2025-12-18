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

public class ListExpenseController {

    @FXML private TableView<ExpenseRecord> listExpenseTableView;
    @FXML private TableColumn<ExpenseRecord, Date> dateTableColumn;
    @FXML private TableColumn<ExpenseRecord, String> categoryTableColumn;
    @FXML private TableColumn<ExpenseRecord, BigDecimal> amountTableColumn;
    @FXML private Button changeButton;
    @FXML private Button deleteButton;
    @FXML private ImageView returnImage;
    @FXML private Button saveButton;

    private ExpenseRecord selectedExpense;
    private final DatabaseHandler dbHandler = new DatabaseHandler();

    @FXML
    void initialize() {
        // Инициализация таблицы
        dateTableColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        categoryTableColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        amountTableColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));

        // Загрузка данных
        loadExpenses();

        // Выбор записи
        listExpenseTableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> selectedExpense = newSelection);

        // Кнопка удаления
        deleteButton.setOnAction(event -> {
            if (selectedExpense != null) {
                try {
                    dbHandler.deleteExpense(selectedExpense.getId());
                    loadExpenses();
                    System.out.println("[EXPENSE] Запись удалена");
                } catch (Exception e) {
                    System.err.println("[ERROR] Ошибка удаления: " + e.getMessage());
                }
            }
        });

        // Кнопка изменения
        changeButton.setOnAction(event -> {
            if (selectedExpense != null) {
                try {
                    // Удаляем выбранную запись
                    dbHandler.deleteExpense(selectedExpense.getId());

                    changeButton.getScene().getWindow().hide();
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(getClass().getResource("changeExpense.fxml"));
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
        returnImage.setOnMouseClicked(event -> switchToExpenseScene());

        saveButton.setOnAction(actionEvent -> {
            saveButton.getScene().getWindow().hide();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("expense.fxml"));
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

    private void loadExpenses() {
        try {
            int userId = AuthorizationController.getCurrentUser().getId();
            ObservableList<ExpenseRecord> expenses = dbHandler.getUserExpenses(userId);
            listExpenseTableView.setItems(expenses);
        } catch (Exception e) {
            System.err.println("[ERROR] Ошибка загрузки расходов: " + e.getMessage());
        }
    }

    private void switchToExpenseScene() {
        try {
            Stage currentStage = (Stage) returnImage.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("expense.fxml"));
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