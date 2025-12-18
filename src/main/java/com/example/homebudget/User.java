package com.example.homebudget;

public class User {
    private int id;
    private String user_name;
    private String login;
    private String password;
    private String email;

    public User(int id, String user_name, String login, String password, String email) {
        this.id = id;
        this.user_name = user_name;
        this.login = login;
        this.password = password;
        this.email = email;
    }

    public User() {
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUserName() { return user_name; }
    public void setUserName(String user_name) { this.user_name = user_name; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}