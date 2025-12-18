package com.example.homebudget;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class CategoryReportController {

    @FXML private TableView<CategoryReport> reportTableView;
    @FXML private TableColumn<CategoryReport, String> categoryTableColumn;
    @FXML private TableColumn<CategoryReport, BigDecimal> expenseTableColumn;
    @FXML private Button createButton;
    @FXML private ImageView returnImage;
    @FXML private ComboBox<SortOption> sortComboBox;

    private final DatabaseHandler dbHandler = new DatabaseHandler();
    private ObservableList<CategoryReport> reportData = FXCollections.observableArrayList();

    @FXML
    void initialize() {
        // Инициализация таблицы
        categoryTableColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        expenseTableColumn.setCellValueFactory(new PropertyValueFactory<>("totalExpense"));
        expenseTableColumn.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", amount));
                }
            }
        });

        // Инициализация ComboBox с вариантами сортировки
        sortComboBox.setItems(FXCollections.observableArrayList(
                new SortOption("Название (А-Я)", Comparator.comparing(CategoryReport::getCategoryName)),
                new SortOption("Название (Я-А)", Comparator.comparing(CategoryReport::getCategoryName).reversed()),
                new SortOption("Сумма (по возрастанию)", Comparator.comparing(CategoryReport::getTotalExpense)),
                new SortOption("Сумма (по убыванию)", Comparator.comparing(CategoryReport::getTotalExpense).reversed())
        ));

        sortComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(SortOption option) {
                return option != null ? option.getDisplayName() : "";
            }

            @Override
            public SortOption fromString(String string) {
                return sortComboBox.getItems().stream()
                        .filter(option -> option.getDisplayName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        sortComboBox.getSelectionModel().selectFirst();

        // Обработчик изменения сортировки
        sortComboBox.setOnAction(event -> {
            SortOption selected = sortComboBox.getValue();
            if (selected != null) {
                reportData.sort(selected.getComparator());
            }
        });

        // Кнопка создания отчета
        createButton.setOnAction(event -> loadReportData());

        // Возврат
        returnImage.setOnMouseClicked(event -> switchToReportScene());


    }

    private void loadReportData() {
        try {
            int userId = AuthorizationController.getCurrentUser().getId();
            Map<String, BigDecimal> expensesByCategory = dbHandler.getExpensesByCategory(userId);

            reportData.clear();
            expensesByCategory.forEach((category, amount) ->
                    reportData.add(new CategoryReport(category, amount))
            );

            // Применяем текущую сортировку
            SortOption selected = sortComboBox.getValue();
            if (selected != null) {
                reportData.sort(selected.getComparator());
            }

            reportTableView.setItems(reportData);

        } catch (SQLException e) {
            System.err.println("Ошибка загрузки отчета: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void exportToExcel() {
        // Реализация экспорта аналогична предыдущему примеру
        System.out.println("Экспорт в Excel...");
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

    // Класс для хранения данных отчета
    public static class CategoryReport {
        private final String categoryName;
        private final BigDecimal totalExpense;

        public CategoryReport(String categoryName, BigDecimal totalExpense) {
            this.categoryName = categoryName;
            this.totalExpense = totalExpense;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public BigDecimal getTotalExpense() {
            return totalExpense;
        }
    }

    // Класс для вариантов сортировки
    private static class SortOption {
        private final String displayName;
        private final Comparator<CategoryReport> comparator;

        public SortOption(String displayName, Comparator<CategoryReport> comparator) {
            this.displayName = displayName;
            this.comparator = comparator;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Comparator<CategoryReport> getComparator() {
            return comparator;
        }
    }
}