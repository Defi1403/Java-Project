package com.example.homebudget;

import java.math.BigDecimal;
import java.sql.Date;

public class IncomeRecord {
    private int id;
    private BigDecimal amount;
    private String category;
    private Date date;

    public IncomeRecord(int id, BigDecimal amount, String category, Date date) {
        this.id = id;
        this.amount = amount;
        this.category = category;
        this.date = date;
    }

    // Геттеры
    public int getId() { return id; }
    public BigDecimal getAmount() { return amount; }
    public String getCategory() { return category; }
    public Date getDate() { return date; }
}