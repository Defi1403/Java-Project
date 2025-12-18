package com.example.homebudget;

import javafx.collections.FXCollections;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class PeriodReportController {

    @FXML private ImageView returnImage;
    @FXML private Button createButton;
    @FXML private ComboBox<String> periodComboBox;
    @FXML private TableView<Map<String, String>> reportTableView;
    @FXML private TableColumn<Map<String, String>, String> incomeColumn;
    @FXML private TableColumn<Map<String, String>, String> expenseColumn;
    @FXML private TableColumn<Map<String, String>, String> balanceColumn;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;

    private final DatabaseHandler dbHandler = new DatabaseHandler();

    @FXML
    void initialize() {
        // Инициализация таблицы
        incomeColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("income")));
        expenseColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("expense")));
        balanceColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("balance")));

        // Настройка ComboBox с периодами
        periodComboBox.setItems(FXCollections.observableArrayList(
                "Сегодня",
                "Неделя",
                "Месяц",
                "Квартал",
                "Год",
                "Произвольный период"
        ));
        periodComboBox.getSelectionModel().selectFirst();

        // Установка текущей даты
        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now());

        // Обработчик выбора периода
        periodComboBox.setOnAction(event -> updatePeriodSelection());

        // Кнопка генерации отчета
        createButton.setOnAction(event -> generateReport());

        // Возврат
        returnImage.setOnMouseClicked(event -> switchToReportScene());
    }

    private void updatePeriodSelection() {
        String selectedPeriod = periodComboBox.getValue();
        LocalDate today = LocalDate.now();

        switch (selectedPeriod) {
            case "Сегодня":
                startDatePicker.setValue(today);
                endDatePicker.setValue(today);
                break;
            case "Неделя":
                startDatePicker.setValue(today.minusWeeks(1));
                endDatePicker.setValue(today);
                break;
            case "Месяц":
                startDatePicker.setValue(today.minusMonths(1));
                endDatePicker.setValue(today);
                break;
            case "Квартал":
                startDatePicker.setValue(today.minusMonths(3));
                endDatePicker.setValue(today);
                break;
            case "Год":
                startDatePicker.setValue(today.minusYears(1));
                endDatePicker.setValue(today);
                break;
            case "Произвольный период":
                // Даты остаются как есть, пользователь сам выберет
                break;
        }
    }

    private void generateReport() {
        try {
            Date startDate = Date.valueOf(startDatePicker.getValue());
            Date endDate = Date.valueOf(endDatePicker.getValue());

            if (startDate.after(endDate)) {
                System.out.println("Ошибка: начальная дата позже конечной");
                return;
            }

            int userId = AuthorizationController.getCurrentUser().getId();

            // Получаем суммы доходов и расходов
            BigDecimal totalIncome = dbHandler.getTotalIncome(userId, startDate, endDate);
            BigDecimal totalExpense = dbHandler.getTotalExpense(userId, startDate, endDate);
            BigDecimal balance = totalIncome.subtract(totalExpense);

            // Форматируем для отображения
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            String period = startDatePicker.getValue().format(formatter) + " - " +
                    endDatePicker.getValue().format(formatter);

            // Создаем данные для таблицы
            Map<String, String> reportData = new HashMap<>();
            reportData.put("period", period);
            reportData.put("income", totalIncome.toString());
            reportData.put("expense", totalExpense.toString());
            reportData.put("balance", balance.toString());

            // Обновляем таблицу
            ObservableList<Map<String, String>> items = FXCollections.observableArrayList();
            items.add(reportData);
            reportTableView.setItems(items);

        } catch (Exception e) {
            System.err.println("Ошибка генерации отчета: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void switchToReportScene() {
        try {
            Stage currentStage = (Stage) returnImage.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("report.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
            currentStage.close();
        } catch (IOException e) {
            System.err.println("Ошибка перехода: " + e.getMessage());
            e.printStackTrace();
        }
    }
}