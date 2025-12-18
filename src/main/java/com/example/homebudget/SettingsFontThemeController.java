package com.example.homebudget;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SettingsFontThemeController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private RadioButton calibriRadioButton;

    @FXML
    private RadioButton darkRadioButton;

    @FXML
    private ToggleGroup fontToggleGroup;

    @FXML
    private RadioButton lightRadioButton;

    @FXML
    private Button saveButton;

    @FXML
    private RadioButton systemRadioButton;

    @FXML
    private ToggleGroup themeToggleGroup;

    @FXML
    void initialize() {
        saveButton.setOnAction(actionEvent -> {
            // Определяем выбранную тему
            boolean isDarkTheme = darkRadioButton.isSelected();
            boolean isLightTheme = lightRadioButton.isSelected();

            // Определяем выбранный шрифт
            boolean isSystemFont = systemRadioButton.isSelected();
            boolean isCalibriFont = calibriRadioButton.isSelected();

            // Определяем какой FXML файл загружать
            String fxmlFile;

            if (isLightTheme && isSystemFont) {
                fxmlFile = "main-menu.fxml";
            } else if (isDarkTheme && isSystemFont) {
                fxmlFile = "main-menuTheme.fxml";
            } else if (isLightTheme && isCalibriFont) {
                fxmlFile = "main-menuFont.fxml";
            } else if (isDarkTheme && isCalibriFont) {
                fxmlFile = "main-menuFontTheme.fxml";
            } else {
                // По умолчанию, если что-то не выбрано
                fxmlFile = "main-menuFontTheme.fxml";
            }

            // Закрываем текущее окно
            saveButton.getScene().getWindow().hide();

            // Загружаем выбранный FXML
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource(fxmlFile));
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