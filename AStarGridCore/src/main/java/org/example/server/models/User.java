package org.example.server.models;

import java.math.BigDecimal;

public class User {
    private String username;
    private String email;
    private BigDecimal balance;

    public User() {
    }

    public User(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", password='******'}"; // Не выводим реальный пароль в логах
    }
}
