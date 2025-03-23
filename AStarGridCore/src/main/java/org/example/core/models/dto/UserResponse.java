package org.example.core.models.dto;

import org.example.core.models.User;

import java.math.BigDecimal;

public class UserResponse {
    private String username;
    private String email;
    private BigDecimal balance;

    public UserResponse() {
    }

    public UserResponse(User user) {
        username = user.getUsername();
        email = user.getEmail();
        balance = user.getBalance();
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
}
