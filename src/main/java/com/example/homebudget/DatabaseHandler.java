package com.example.homebudget;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHandler extends Configs {
    private Connection dbConnection;

    public Connection getDbConnection() throws SQLException, ClassNotFoundException {
        String connectionString = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName +
                "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

        // Для MySQL 8.0+ используйте:
        Class.forName("com.mysql.cj.jdbc.Driver");

        // Для MySQL 5.x используйте:
        // Class.forName("com.mysql.jdbc.Driver");

        return DriverManager.getConnection(connectionString, dbUser, dbPass);
    }

    public Map<String, BigDecimal> getExpensesByCategory(int userId) throws SQLException {
        Map<String, BigDecimal> result = new HashMap<>();
        String query = "SELECT category_name, SUM(amount) as total FROM Expense WHERE user_id = ? GROUP BY category_name";

        try (Connection conn = getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                result.put(
                        rs.getString("category_name"),
                        rs.getBigDecimal("total")
                );
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Ошибка при получении доходов: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    public BigDecimal getTotalIncome(int userId, Date startDate, Date endDate) throws SQLException {
        String query = "SELECT COALESCE(SUM(amount), 0) FROM Income WHERE user_id = ? AND date BETWEEN ? AND ?";
        try (Connection conn = getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setDate(2, startDate);
            stmt.setDate(3, endDate);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Ошибка при получении доходов: " + e.getMessage());
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }

    public BigDecimal getTotalExpense(int userId, Date startDate, Date endDate) throws SQLException {
        String query = "SELECT COALESCE(SUM(amount), 0) FROM Expense WHERE user_id = ? AND date BETWEEN ? AND ?";
        try (Connection conn = getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setDate(2, startDate);
            stmt.setDate(3, endDate);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Ошибка при получении расходов: " + e.getMessage());
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }

    public ObservableList<CategoryRecord> getListCategories() throws SQLException, ClassNotFoundException {
        ObservableList<CategoryRecord> categories = FXCollections.observableArrayList();
        String query = "SELECT category_name FROM Categories";

        try (Connection connection = getDbConnection();
             PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                categories.add(new CategoryRecord(rs.getString("category_name"))); // Исправлено здесь
            }
        }
        return categories;
    }

    public void deleteCategory(String categoryName) throws SQLException, ClassNotFoundException {
        // Сначала удаляем все записи доходов и расходов с этой категорией
        String deleteIncomeQuery = "DELETE FROM Income WHERE category_name = ?";
        String deleteExpenseQuery = "DELETE FROM Expense WHERE category_name = ?";
        String deleteCategoryQuery = "DELETE FROM Categories WHERE category_name = ?";

        try (Connection connection = getDbConnection()) {
            connection.setAutoCommit(false); // Начинаем транзакцию

            try {
                // Удаляем связанные доходы
                try (PreparedStatement stmt = connection.prepareStatement(deleteIncomeQuery)) {
                    stmt.setString(1, categoryName);
                    stmt.executeUpdate();
                }

                // Удаляем связанные расходы
                try (PreparedStatement stmt = connection.prepareStatement(deleteExpenseQuery)) {
                    stmt.setString(1, categoryName);
                    stmt.executeUpdate();
                }

                // Удаляем саму категорию
                try (PreparedStatement stmt = connection.prepareStatement(deleteCategoryQuery)) {
                    stmt.setString(1, categoryName);
                    stmt.executeUpdate();
                }

                connection.commit(); // Фиксируем транзакцию
            } catch (SQLException e) {
                connection.rollback(); // Откатываем в случае ошибки
                throw e;
            } finally {
                connection.setAutoCommit(true); // Возвращаем авто-коммит
            }
        }
    }

    public ObservableList<IncomeRecord> getUserIncomes(int userId) throws SQLException, ClassNotFoundException {
        ObservableList<IncomeRecord> incomes = FXCollections.observableArrayList();
        String query = "SELECT income_id, amount, category_name, date FROM Income WHERE user_id = ? ORDER BY date DESC";

        try (Connection connection = getDbConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    incomes.add(new IncomeRecord(
                            rs.getInt("income_id"),
                            rs.getBigDecimal("amount"),
                            rs.getString("category_name"),
                            rs.getDate("date")
                    ));
                }
            }
        }
        return incomes;
    }

    public ObservableList<ExpenseRecord> getUserExpenses(int userId) throws SQLException, ClassNotFoundException {
        ObservableList<ExpenseRecord> expenses = FXCollections.observableArrayList();
        String query = "SELECT expense_id, amount, category_name, date FROM Expense WHERE user_id = ? ORDER BY date DESC";

        try (Connection connection = getDbConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    expenses.add(new ExpenseRecord(
                            rs.getInt("expense_id"),
                            rs.getBigDecimal("amount"),
                            rs.getString("category_name"),
                            rs.getDate("date")
                    ));
                }
            }
        }
        return expenses;
    }

    public void deleteIncome(int incomeId) throws SQLException, ClassNotFoundException {
        String query = "DELETE FROM Income WHERE income_id = ?";

        try (Connection connection = getDbConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setInt(1, incomeId);
            stmt.executeUpdate();
        }
    }

    public void deleteExpense(int expenseId) throws SQLException, ClassNotFoundException {
        String query = "DELETE FROM Expense WHERE expense_id = ?";

        try (Connection connection = getDbConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setInt(1, expenseId);
            stmt.executeUpdate();
        }
    }

    public int regUser(User user) {
        String insert = "INSERT INTO Users (user_name, login, password, email) VALUES (?, ?, ?, ?)";

        try (Connection connection = getDbConnection();
             PreparedStatement prSt = connection.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {

            prSt.setString(1, user.getUserName());
            prSt.setString(2, user.getLogin());
            prSt.setString(3, user.getPassword());
            prSt.setString(4, user.getEmail());
            prSt.executeUpdate();

            try (ResultSet rs = prSt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public User getUser(String login, String password) {
        String select = "SELECT * FROM Users WHERE login = ? AND password = ?";

        try (Connection connection = getDbConnection();
             PreparedStatement prSt = connection.prepareStatement(select)) {

            prSt.setString(1, login);
            prSt.setString(2, password);

            try (ResultSet resSet = prSt.executeQuery()) {
                if (resSet.next()) {
                    return new User(
                            resSet.getInt("user_id"),
                            resSet.getString("user_name"),
                            resSet.getString("login"),
                            resSet.getString("password"),
                            resSet.getString("email")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addCategory(String categoryName) {
        String insert = "INSERT INTO Categories (category_name) VALUES (?)";
        try (Connection connection = getDbConnection();
             PreparedStatement prSt = connection.prepareStatement(insert)) {
            prSt.setString(1, categoryName);
            prSt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> getCategories() {
        List<String> categories = new ArrayList<>();
        String query = "SELECT category_name FROM Categories";

        try (Connection connection = getDbConnection();
             PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                categories.add(rs.getString("category_name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return categories;
    }

    public void addExpense(int userId, BigDecimal amount, String categoryName, Date date) {
        String insert = "INSERT INTO Expense (user_id, amount, date, category_name) VALUES (?, ?, ?, ?)";
        executeInsert(insert, userId, amount, date, categoryName);
    }

    public void addIncome(int userId, BigDecimal amount, String categoryName, Date date) {
        String insert = "INSERT INTO Income (user_id, amount, date, category_name) VALUES (?, ?, ?, ?)";
        executeInsert(insert, userId, amount, date, categoryName);
    }

    private void executeInsert(String query, int userId, BigDecimal amount, Date date, String categoryName) {
        try (Connection connection = getDbConnection();
             PreparedStatement prSt = connection.prepareStatement(query)) {
            prSt.setInt(1, userId);
            prSt.setBigDecimal(2, amount);
            prSt.setDate(3, date);
            prSt.setString(4, categoryName);
            prSt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}