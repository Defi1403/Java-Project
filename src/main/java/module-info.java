module com.example.homebudget {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.prefs;
    requires java.sql;
    requires mysql.connector.j;

    opens com.example.homebudget to javafx.fxml;
    exports com.example.homebudget;
}