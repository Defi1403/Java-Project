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

public class ListCategoryController {

    @FXML private TableView<CategoryRecord> listCategoryTableView;
    @FXML private TableColumn<CategoryRecord, String> categoryTableColumn;
    @FXML private Button changeButton;
    @FXML private Button deleteButton;
    @FXML private ImageView returnImage;
    @FXML private Button saveButton;

    private CategoryRecord selectedCategory;
    private final DatabaseHandler dbHandler = new DatabaseHandler();

    @FXML
    void initialize() {
        // Настройка таблицы
        categoryTableColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        loadCategories();

        // Обработчик выбора категории
        listCategoryTableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> selectedCategory = newSelection);

        // Удаление категории
        deleteButton.setOnAction(event -> {
            if (selectedCategory != null) {
                try {
                    dbHandler.deleteCategory(selectedCategory.getCategoryName());
                    System.out.println("Удалена категория: " + selectedCategory.getCategoryName());
                    loadCategories();
                } catch (Exception e) {
                    System.err.println("Ошибка удаления: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        // Изменение категории (удаление + переход)
        changeButton.setOnAction(event -> {
            if (selectedCategory != null) {
                try {
                    // 1. Удаляем выбранную категорию
                    dbHandler.deleteCategory(selectedCategory.getCategoryName());
                    changeButton.getScene().getWindow().hide();
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(getClass().getResource("changeCategory.fxml"));
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
                    System.err.println("Ошибка при изменении: " + e.getMessage());
                }
            }
        });

        // Возврат
        returnImage.setOnMouseClicked(event -> switchToPreviousScene());
        saveButton.setOnAction(event -> switchToPreviousScene());
    }

    private void loadCategories() {
        try {
            ObservableList<CategoryRecord> categories = dbHandler.getListCategories();
            listCategoryTableView.setItems(categories);
        } catch (Exception e) {
            System.err.println("Ошибка загрузки категорий: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void switchToPreviousScene() {
        try {
            Stage currentStage = (Stage) returnImage.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("category.fxml"));
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